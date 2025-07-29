// IbanConfirmationViewModel.kt
package com.example.national_iban_recognition_component.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.national_iban_recognition_component.model.IbanCategory // YENİ IMPORT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class IbanConfirmationViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    // Navigasyon argümanlarını al
    private val _countryCode = MutableStateFlow(savedStateHandle.get<String>("countryCode") ?: "TR")
    val countryCode: StateFlow<String> = _countryCode.asStateFlow()

    private val _iban = MutableStateFlow(savedStateHandle.get<String>("iban") ?: "")
    val iban: StateFlow<String> = _iban.asStateFlow()

    // YENİ EKLENEN STATE'LER
    private val _ownerFullName = MutableStateFlow(savedStateHandle.get<String>("ownerFullName") ?: "")
    val ownerFullName: StateFlow<String> = _ownerFullName.asStateFlow()

    private val _shortName = MutableStateFlow(savedStateHandle.get<String>("shortName") ?: "")
    val shortName: StateFlow<String> = _shortName.asStateFlow()

    // Enum olarak alırken name() ile gönderip fromString() ile dönüştürüyoruz
    private val _category = MutableStateFlow(
        IbanCategory.valueOf(savedStateHandle.get<String>("category") ?: IbanCategory.NONE.name)
    )
    val category: StateFlow<IbanCategory> = _category.asStateFlow()

    // Henüz bir işlem yok, ancak gelecekte kaydetme, düzenleme vb. mantık buraya gelebilir.
    fun onConfirmClicked() {
        // IBAN bilgilerini kaydetme veya başka bir işlem başlatma mantığı buraya gelecek
        // Örneğin, bir veritabanına kaydetme veya bir API'ye gönderme.
        // Şimdilik sadece loglayabiliriz veya bir Toast gösterebiliriz.
        val confirmedIbanInfo = "IBAN: ${_iban.value}, Ülke Kodu: ${_countryCode.value}, " +
                "Hesap Sahibi: ${_ownerFullName.value}, Kısa İsim: ${_shortName.value}, " +
                "Kategori: ${_category.value.displayName}" // Enum'un görünen adını kullan
        println("Onaylandı: $confirmedIbanInfo") // Konsola yazdır

        // Başarılı bir şekilde kaydedildiğine dair bir geri bildirim ve ana ekrana dönüş
        // Örneğin: _toastMessage.emit("IBAN başarıyla kaydedildi!")
        // veya navController.popBackStack()
    }
}