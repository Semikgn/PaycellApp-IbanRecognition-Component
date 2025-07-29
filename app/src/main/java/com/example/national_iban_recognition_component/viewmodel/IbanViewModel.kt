// IbanViewModel.kt
package com.example.national_iban_recognition_component.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.national_iban_recognition_component.model.IbanCategory // YENİ IMPORT
import com.example.national_iban_recognition_component.model.SelectedIbanInfo
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IbanViewModel(application: Application) : AndroidViewModel(application) {
    val ibanConfigs = listOf(
        "TR" to 24,
        "GB" to 20,
        "FR" to 25,
        "DE" to 20
    )

    val ibanRegexMap = mapOf(
        // Başında ve sonunda ^ ve $ olmamasına dikkat edin, zira find/findAll kullanıyoruz.
        "TR" to Regex("TR\\d{2}[A-Z0-9]{4}\\d{16}[A-Z0-9]{2}"),
        "GB" to Regex("GB\\d{2}[A-Z]{4}\\d{14}"),
        "FR" to Regex("FR\\d{2}[A-Z0-9]{23}"),
        "DE" to Regex("DE\\d{2}[A-Z0-9]{18}")
    )

    private val _selectedCountryCode = MutableStateFlow(ibanConfigs.first().first)
    val selectedCountryCode: StateFlow<String> = _selectedCountryCode.asStateFlow()

    private val _currentIbanText = MutableStateFlow("")
    val currentIbanText: StateFlow<String> = _currentIbanText.asStateFlow()

    // OLMADAN ÖNCE:
    // private val _firstName = MutableStateFlow("")
    // val firsName: StateFlow<String> = _firstName.asStateFlow()
    // private val _lastName = MutableStateFlow("")
    // val lastName: StateFlow<String> = _lastName.asStateFlow()

    // YENİ EKLENEN STATE'LER (Ad, Soyad yerine Tek İsim, Kısa İsim, Kategori)
    private val _ownerFullName = MutableStateFlow("")
    val ownerFullName: StateFlow<String> = _ownerFullName.asStateFlow()

    private val _shortName = MutableStateFlow("")
    val shortName: StateFlow<String> = _shortName.asStateFlow()

    private val _selectedCategory = MutableStateFlow(IbanCategory.NONE) // Varsayılan olarak "Kategori Seç"
    val selectedCategory: StateFlow<IbanCategory> = _selectedCategory.asStateFlow()

    private val _navigateToConfirmationEvent = MutableSharedFlow<SelectedIbanInfo>()
    val navigateToConfirmationEvent: SharedFlow<SelectedIbanInfo> = _navigateToConfirmationEvent.asSharedFlow()

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val _detectedIbans = MutableStateFlow<List<String>>(emptyList())
    val detectedIbans: StateFlow<List<String>> = _detectedIbans.asStateFlow()

    private val _showIbanBottomSheet = MutableStateFlow(false)
    val showIbanBottomSheet: StateFlow<Boolean> = _showIbanBottomSheet.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private val _ibanError = MutableStateFlow<String?>(null)
    val ibanError: StateFlow<String?> = _ibanError.asStateFlow()


    fun onCountrySelected(code: String) {
        _selectedCountryCode.value = code
        _currentIbanText.value = ""
        _ibanError.value = null
    }

    fun onIbanTextChanged(newText: String) {
        val currentConfig = ibanConfigs.firstOrNull { it.first == _selectedCountryCode.value }
        val currentAccountMaxLength = currentConfig?.second ?: 0
        val currentFullMaxLen = _selectedCountryCode.value.length + currentAccountMaxLength

        if (newText.length <= currentFullMaxLen) {
            _currentIbanText.value = newText
        } else {
            _ibanError.value = "IBAN uzunluğu ${currentFullMaxLen} karakteri aşıyor."
        }
        _ibanError.value = validateIbanFormat(_selectedCountryCode.value, newText)
    }

    // YENİ EKLENEN FONKSİYONLAR
    fun onOwnerFullNameChanged(newText: String) {
        _ownerFullName.value = newText
    }

    fun onShortNameChanged(newText: String) {
        _shortName.value = newText
    }

    fun onCategorySelected(category: IbanCategory) {
        _selectedCategory.value = category
    }

    private fun validateIbanFormat(countryCode: String, ibanText: String): String? {
        val ibanConfig = ibanConfigs.find { it.first == countryCode }
        val regex = ibanRegexMap[countryCode]
        val maxLen = ibanConfig?.second ?: 0
        val fullExpectedLen = countryCode.length + maxLen

        return when {
            ibanText.isBlank() -> null
            ibanText.length < fullExpectedLen -> "IBAN uzunluğu ${fullExpectedLen} karakter olmalıdır."
            ibanText.length > fullExpectedLen -> "IBAN uzunluğu ${fullExpectedLen} karakteri aşıyor."
            regex != null && !regex.matches(ibanText) -> "Geçersiz IBAN formatı."
            else -> null
        }
    }

    fun onScanClicked() {
        viewModelScope.launch {
            _detectedIbans.value = emptyList()
            _showIbanBottomSheet.value = false
            _toastMessage.emit("Kamera izni isteniyor...")
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
                    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, _photoUri.value)
                    if (bitmap == null){
                        _toastMessage.emit("Resim alınamadı, lütfen tekrar deneyin.")
                        return@launch
                    }
                    processImageForIban(bitmap, context)
                } catch (e: Exception) {
                    _toastMessage.emit("Resim işlenirken hata oluştu: ${e.localizedMessage}")
                }
            } else {
                _toastMessage.emit("Resim çekme işlemi iptal edildi veya hata oluştu.")
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
                            val flexibleRegex = Regex(baseRegexPattern) // ^ ve $ olmadan

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
                _detectedIbans.value = foundIbans
                Log.d("IbanViewModel", "Sonuç: Bulunan benzersiz IBAN'lar: $foundIbans")

                viewModelScope.launch {
                    if (foundIbans.isNotEmpty()) {
                        _showIbanBottomSheet.value = true
                    } else {
                        _toastMessage.emit("Resimde geçerli IBAN bulunamadı.")
                        _showIbanBottomSheet.value = false
                    }
                }
            }
            .addOnFailureListener { e ->
                viewModelScope.launch {
                    _toastMessage.emit("IBAN tanıma başarısız oldu: ${e.localizedMessage}")
                    _showIbanBottomSheet.value = false
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
            _selectedCountryCode.value = countryCodeFromIban
            _currentIbanText.value = selectedIban
            _ibanError.value = validateIbanFormat(countryCodeFromIban, selectedIban)
            _showIbanBottomSheet.value = false
            Log.d("IbanViewModel", "IBAN ve ülke kodu güncellendi: $selectedIban, $countryCodeFromIban")
        } else {
            viewModelScope.launch {
                _toastMessage.emit("Seçilen IBAN (${countryCodeFromIban}), desteklenen ülkelerden birine ait değil!")
                _showIbanBottomSheet.value = false
            }
        }
    }

    fun onBottomSheetDismissed() {
        _showIbanBottomSheet.value = false
        _detectedIbans.value = emptyList()
        viewModelScope.launch {
            _toastMessage.emit("IBAN seçimi iptal edildi.")
        }
    }

    fun notifyInvalidCountryCodeSelected() {
        viewModelScope.launch {
            _toastMessage.emit("Seçilen IBAN, desteklenen ülkelerden birine ait değil!")
        }
    }

    fun notifyNoIbanSelected() {
        viewModelScope.launch {
            _toastMessage.emit("Lütfen bir IBAN seçin.")
        }
    }

    fun onContinueClicked() {
        val country = _selectedCountryCode.value
        val iban = _currentIbanText.value
        // OLMADAN ÖNCE:
        // val currentFirstName = _firstName.value
        // val currentLastName = _lastName.value

        // YENİ ALANLAR
        val currentOwnerFullName = _ownerFullName.value
        val currentShortName = _shortName.value
        val currentCategory = _selectedCategory.value

        val ibanError = validateIbanFormat(country, iban)

        // Devam Et butonu için doğrulama mantığını güncelle
        if (iban.isNotBlank() &&
            ibanError == null && // IBAN'da hata yoksa
            currentOwnerFullName.isNotBlank() && // Hesap Sahibi Adı boş olmamalı
            currentShortName.isNotBlank() // Kısa İsim boş olmamalı
        // Kategori opsiyonel olduğu için burada kontrol etmiyoruz
        ) {
            val selectedIbanInfo = SelectedIbanInfo(
                countryCode = country,
                iban = iban,
                ownerFullName = currentOwnerFullName, // Yeni alan
                shortName = currentShortName,         // Yeni alan
                category = currentCategory            // Yeni alan
            )
            viewModelScope.launch {
                _navigateToConfirmationEvent.emit(selectedIbanInfo)
            }
        } else {
            viewModelScope.launch {
                _toastMessage.emit("Lütfen IBAN, Hesap Sahibi Adı ve Kısa İsim alanlarını eksiksiz doldurun.")
            }
        }
    }
}