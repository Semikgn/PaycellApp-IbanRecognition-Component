package com.example.national_iban_recognition_component.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import com.example.national_iban_recognition_component.model.SelectedIbanInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Locale

class IbanViewModel(application: Application) : AndroidViewModel(application) {
    val ibanConfigs = listOf(
        "TR" to 24,
        "GB" to 20,
        "FR" to 25,
        "DE" to 20
    )
    /*Her ülke için farklı regex yapısı
    val ibanRegexMap = mapOf(
        "TR" to Regex("TR\\d{2}[A-Z0-9]{4}\\d{16}[A-Z0-9]{2}"),
        "GB" to Regex("GB\\d{2}[A-Z]{4}\\d{14}"),
        "FR" to Regex("FR\\d{2}[A-Z0-9]{23}"),
        "DE" to Regex("DE\\d{2}[A-Z0-9]{18}")
    )*/
    /*private  val _ibanErrorMessages = ibanConfigs.map { ibanConfig ->
        ibanConfig.first to MutableStateFlow<String?>(null) }.toMap()
    val ibanErrorMessages: Map<String, StateFlow<String?>> = _ibanErrorMessages
*/
    //Seçilen ülke kodunu tut
    private val _selectedCountryCode = MutableStateFlow(ibanConfigs.first().first)
    val selectedCountryCode: StateFlow<String> = _selectedCountryCode.asStateFlow()

    //Seçilen ülkenin iban fieldını tut
    private val _currentIbanText = MutableStateFlow("")
    val currentIbanText: StateFlow<String> = _currentIbanText.asStateFlow()

    //Girilen isim-soyismi tut
    private val _firstName = MutableStateFlow("")
    val firsName: StateFlow<String> = _firstName.asStateFlow()

    private val _lastName = MutableStateFlow("")
    val lastName: StateFlow<String> = _lastName.asStateFlow()

    private val _navigateToConfirmationEvent = MutableSharedFlow<SelectedIbanInfo>()
    val navigateToConfirmationEvent: SharedFlow<SelectedIbanInfo> = _navigateToConfirmationEvent.asSharedFlow()

    private val _activeScanIndex = MutableStateFlow<Int?>(null)
    val activeScanIndex: StateFlow<Int?> = _activeScanIndex.asStateFlow() //Iban alanı scan edilecek bu index izlenerek kamera tetiklenecek

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow() //uri -> viewmodel iletir o da temp fotoğrafı işler

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun onCountrySelected(code: String) {
        _selectedCountryCode.value = code
        _currentIbanText.value = ""
    }

    fun onIbanTextChanged(newText: String) {
        val maxLength = ibanConfigs.firstOrNull { it.first == _selectedCountryCode.value }?.second ?: 0
        if (newText.length <= maxLength) {
            _currentIbanText.value = newText
        }
    }
    /*private fun validateIban(countryCode: String, ibanText: String) {
        val ibanConfig = ibanConfigs.find { it.first == countryCode }
        val regex = ibanRegexMap[countryCode]
        val maxLen = ibanConfig?.second ?: 0
        var errorMessage: String? = null

        if (ibanText.isBlank()){
            errorMessage = null
        }else if (ibanText.length < maxLen){
            errorMessage = "IBAN uzunluğu ${maxLen} karakter olmalıdır."
        } else if (regex != null && !ibanText.matches(regex)) {
            errorMessage = "Geçersiz format"
        } else {
            errorMessage = null
        }

    }*/

    fun onFirstNameChanged(newText: String){
        _firstName.value = newText
    }
    fun onLastNameChanged(newText: String){
        _lastName.value = newText
    }

    //Tarama butonuna tıklandığında
    fun onScanClicked() {
        val index = ibanConfigs.indexOfFirst { it.first == _selectedCountryCode.value }
        if(index != -1) {
            _activeScanIndex.value = index
            Log.d("IbanViewModel", "Tarama başlatma isteği (Ülke: ${_selectedCountryCode.value})")
        } else {
            Log.e("IbanViewModel", "Seçilen ülke kodu için IBAN konfigürasyonu bulunamadı: ${_selectedCountryCode.value}")
        }
    }
    //Fotonun kaydedileceği uri nesnesini viewmodela iletir
    fun onPhotoUriCreated(uri: Uri) {
        _photoUri.value = uri
        Log.d("IbanViewModel", "Fotoğraf URI'si ViewModel'a iletildi: $uri")
    }
    //Kameradan fotoğraf çekme işlemi sonucu
    fun onPictureTakenResult(success: Boolean, context: Context) {
        viewModelScope.launch {
            if (success && _photoUri.value != null) { //Çekilen fotoğraf null değilse
                try {
                    //get bitmap uriden
                    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, _photoUri.value)
                    if (bitmap == null){
                        _activeScanIndex.value = null //reset index
                        return@launch
                        Log.d("IbanViewModel", "Bitmap null döndü")
                    }
                    processImageForIban(bitmap, context)
                } catch (e: Exception) {
                    //TODO: Buraya Toast
                    Log.d("IbanViewModel", "Resim işlenirken veya bitmap okunurken hata oluştu.")
                    _activeScanIndex.value = null
                }
            } else {
                //TODO: Buraya Toast
                Log.d("IbanViewModel", "Resim çekme işlemi iptal edildi veya hata oluştu.")
                _activeScanIndex.value = null
            }
        }
    }
    private fun processImageForIban(bitmap: Bitmap, context: Context) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val rawText = visionText.text.replace("\\s".toRegex(), "")
                val foundIban = Regex("([A-Z]{2}\\d{2}[A-Z0-9]{13,30})").find(rawText)?.value
                Log.d("IbanViewModel", "Regex ile bulunan IBAN: $foundIban")

                if (foundIban != null) {
                    val currentCountryCode = _selectedCountryCode.value
                    val config = ibanConfigs.firstOrNull() { it.first == currentCountryCode }

                        if (config != null && foundIban.startsWith(currentCountryCode)) {
                            val expectedLenght = config.second
                            val finalIban = if (foundIban.length > (currentCountryCode.length + expectedLenght)) {
                                foundIban.substring(0, currentCountryCode.length + expectedLenght)
                            } else{
                                foundIban
                            }
                            _currentIbanText.value = finalIban //state günceller ve ibanı kaydeder
                            //TODO: Buraya Toast
                        }
                        else{
                            // TODO: UI'ya "Farklı bir ülkenin IBAN'ı bulundu." Toast'ı koy
                        }
                } else{
                    // TODO: UI'ya "Resimde IBAN bulunamadı." Toast'ı koy
                }
            }
            .addOnFailureListener { e ->
                // TODO: UI'ya "IBAN tanıma başarısız oldu."
            }
            .addOnCompleteListener {
                Log.d("IbanViewModel", "Metin tanıma işlemi tamamlandı.")
                _activeScanIndex.value = null
            }
    }

    fun onContinueClicked() {
        val country = _selectedCountryCode.value
        val iban = _currentIbanText.value
        val currentFirstName = _firstName.value
        val currentLastName = _lastName.value

        if (iban.isNotBlank() && currentFirstName.isNotBlank() && currentLastName.isNotBlank()) {
            val selectedIbanInfo = SelectedIbanInfo(country, iban, currentFirstName, currentLastName)
            viewModelScope.launch {
                _navigateToConfirmationEvent.emit(selectedIbanInfo)
            }
        } else {
            Log.w("IbanViewModel", "Boşluk kalan kutucuklar var.")
            //TODO: Buraya Toast yapıcam !!
        }
    }
}