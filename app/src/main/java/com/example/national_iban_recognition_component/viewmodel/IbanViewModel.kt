// viewmodel/IbanViewModel.kt
package com.example.national_iban_recognition_component.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.national_iban_recognition_component.model.IbanRecognitionUiState
import com.example.national_iban_recognition_component.model.SelectedIbanInfo
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class IbanInputState(
    val text: String = "",
    val error: String? = null
)

class IbanViewModel(application: Application) : AndroidViewModel(application) {
    val ibanConfigs = listOf(
        "TR" to 24,
        "GB" to 20,
        "FR" to 25,
        "DE" to 20
    )

    val ibanRegexMap = mapOf(
        "TR" to Regex("TR\\d{2}[A-Z0-9]{4}\\d{16}[A-Z0-9]{2}"),
        "GB" to Regex("GB\\d{2}[A-Z]{4}\\d{14}"),
        "FR" to Regex("FR\\d{2}[A-Z0-9]{23}"),
        "DE" to Regex("DE\\d{2}[A-Z0-9]{18}")
    )
//UI State class
    private val _uiState = MutableStateFlow(
        IbanRecognitionUiState(
            ibanInputStates = ibanConfigs.associate { (code, _) -> code to IbanInputState() }
        )
    )
    val uiState: StateFlow<IbanRecognitionUiState> = _uiState.asStateFlow()

    private val _navigateToConfirmationEvent = MutableSharedFlow<SelectedIbanInfo>()
    val navigateToConfirmationEvent: SharedFlow<SelectedIbanInfo> = _navigateToConfirmationEvent.asSharedFlow()

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun onBottomSheetIbanSelected(iban: String) {
        _uiState.value = _uiState.value.copy(selectedIbanInBottomSheet = iban)
    }

    // BottomSheet kapandığında seçimi sıfırlama fonksiyonu
    fun onBottomSheetDismissed() {
        _uiState.value = _uiState.value.copy(showIbanBottomSheet = false, selectedIbanInBottomSheet = null)
    }

    fun onIbanTextChanged(countryCode: String, newText: String) {
        val currentConfig = ibanConfigs.firstOrNull { it.first == countryCode }
        val currentFullMaxLen = (currentConfig?.second ?: 0) + countryCode.length

        // Metnin uzunluğunu kontrol et
        val processedText = if (newText.length <= currentFullMaxLen) {
            newText
        } else {
            // Aşılırsa sadece sınıra kadar olan kısmı al
            newText.take(currentFullMaxLen)
        }
        val error = validateIbanFormat(countryCode, processedText)

        val updatedStates = _uiState.value.ibanInputStates.toMutableMap().apply {
            this[countryCode] = IbanInputState(text = processedText, error = error)
        }
        _uiState.value = _uiState.value.copy(ibanInputStates = updatedStates)
    }

    private fun validateIbanFormat(countryCode: String, ibanText: String): String? {
        val ibanConfig = ibanConfigs.find { it.first == countryCode }
        val regex = ibanRegexMap[countryCode]
        val maxLen = ibanConfig?.second ?: 0
        val fullExpectedLen = countryCode.length + maxLen

        // Eğer IBAN metni hala ülke kodu ile başlamıyorsa, regex kontrolünü yapma
        if (!ibanText.startsWith(countryCode, ignoreCase = true) && ibanText.length >= countryCode.length) {
            return "IBAN '$countryCode' ile başlamalıdır."
        }

        return when {
            ibanText.isBlank() -> null // Boşsa hata gösterme
            ibanText.length < fullExpectedLen -> "IBAN uzunluğu ${fullExpectedLen} karakter olmalıdır."
            ibanText.length > fullExpectedLen -> "IBAN uzunluğu ${fullExpectedLen} karakteri aşıyor."
            regex != null && !regex.matches(ibanText) -> "Geçersiz IBAN formatı."
            else -> null
        }
    }

    fun onScanClicked(countryCodeForScan: String) { // Hangi IBAN alanı için tarama yapıldığını belirt
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(detectedIbans = emptyList())
            _uiState.value = _uiState.value.copy(showIbanBottomSheet = false)
            _uiState.value = _uiState.value.copy(toastMessage = "Kamera izni isteniyor...")
        }
    }

    fun onPhotoUriCreated(uri: Uri) {
        _photoUri.value = uri
        Log.d("IbanViewModel", "Fotoğraf URI'si ViewModel'a iletildi: $uri")
    }

    fun onPictureTakenResult(success: Boolean, context: Context) {
        viewModelScope.launch {
            if (success && _photoUri.value != null) {
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, _photoUri.value)
                    }
                    if (bitmap == null){
                        _uiState.value = _uiState.value.copy(toastMessage = "Resim alınamadı, lütfen tekrar deneyin.")
                        return@launch
                    }
                    processImageForIban(bitmap, context)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(toastMessage = "Resim işlenirken hata oluştu: ${e.localizedMessage}")
                }
            } else {
                _uiState.value = _uiState.value.copy(toastMessage = "Resim çekme işlemi iptal edildi veya hata oluştu.")
            }
        }
    }

    private fun processImageForIban(bitmap: Bitmap, context: Context) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val foundIbansSet = mutableSetOf<String>()

                for (block in visionText.textBlocks) {
                    val cleanedBlockText = block.text.replace("\\s".toRegex(), "").uppercase()
                    Log.d("IbanViewModel", "İşlenen blok metni: $cleanedBlockText")

                    for ((countryCodePrefix, expectedLength) in ibanConfigs) {
                        val fullExpectedLen = countryCodePrefix.length + expectedLength
                        val baseRegexPattern = ibanRegexMap[countryCodePrefix]?.pattern

                        if (baseRegexPattern != null) {
                            val flexibleRegex = Regex(baseRegexPattern)

                            flexibleRegex.findAll(cleanedBlockText).forEach { matchResult ->
                                val potentialIban = matchResult.value

                                if (potentialIban.length == fullExpectedLen && validateIbanFormat(countryCodePrefix, potentialIban) == null) {
                                    foundIbansSet.add(potentialIban)
                                    Log.d("IbanViewModel", "Blokta bulunan ve doğrulanan IBAN: $potentialIban")
                                }
                            }
                        }
                    }
                }

                val foundIbans = foundIbansSet.toList()
                Log.d("IbanViewModel", "Sonuç: Bulunan benzersiz IBAN'lar: $foundIbans")

                viewModelScope.launch {
                    if (foundIbans.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(detectedIbans = foundIbans, showIbanBottomSheet = true)
                    } else {
                        _uiState.value = _uiState.value.copy(toastMessage ="Resimde geçerli IBAN bulunamadı.", showIbanBottomSheet = false)
                    }
                }
            }
            .addOnFailureListener { e ->
                viewModelScope.launch {
                    _uiState.value = _uiState.value.copy(toastMessage = "IBAN tanıma başarısız oldu: ${e.localizedMessage}", showIbanBottomSheet = false )
                }
            }
            .addOnCompleteListener {
                Log.d("IbanViewModel", "Metin tanıma işlemi tamamlandı.")
            }
    }

    fun onIbanSelectedFromBottomSheet(selectedIban: String) {
        val countryCodeFromIban = selectedIban.take(2).uppercase()
        val isValidCountry = ibanConfigs.any { it.first == countryCodeFromIban }

        if (isValidCountry) {
            val updatedStates = _uiState.value.ibanInputStates.toMutableMap().apply {
                this[countryCodeFromIban] = IbanInputState(text = selectedIban, error = validateIbanFormat(countryCodeFromIban, selectedIban))
            }
            _uiState.value = _uiState.value.copy(ibanInputStates = updatedStates, showIbanBottomSheet = false)
            Log.d("IbanViewModel", "IBAN ve ülke kodu güncellendi: $selectedIban, $countryCodeFromIban")
        } else {
            _uiState.value = _uiState.value.copy(toastMessage = "Seçilen IBAN (${countryCodeFromIban}), desteklenen ülkelerden birine ait değil!", showIbanBottomSheet = false)
        }
    }

    fun onContinueClicked() {
        val validIbanEntry = _uiState.value.ibanInputStates.entries.firstOrNull { (_, state) ->
            state.text.isNotBlank() && state.error == null
        }

        if (validIbanEntry != null) {
            val country = validIbanEntry.key
            val ibanText = validIbanEntry.value.text

            val selectedIbanInfo = SelectedIbanInfo(
                countryCode = country,
                iban = ibanText
            )

            viewModelScope.launch {
                _navigateToConfirmationEvent.emit(selectedIbanInfo)
            }
        } else {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(toastMessage = "Lütfen geçerli bir IBAN girin.")
            }
        }
    }
}