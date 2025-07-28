package com.example.national_iban_recognition_component.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.national_iban_recognition_component.model.SelectedIbanInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log


class IbanConfirmationViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _selectedIbanInfo = MutableStateFlow(
        SelectedIbanInfo(
            countryCode = savedStateHandle.get<String>("countryCode") ?: "",
            iban = savedStateHandle.get<String>("iban") ?: "",
            firstName = savedStateHandle.get<String>("firstName") ?: "",
            lastName = savedStateHandle.get<String>("lastName") ?: ""
        )
    )
    val selectedIbanInfo: StateFlow<SelectedIbanInfo> = _selectedIbanInfo.asStateFlow()

    init {
        Log.d("IbanConfViewModel", "Gelen IBAN Bilgisi: ${_selectedIbanInfo.value.countryCode} / ${_selectedIbanInfo.value.iban}")
    }

     fun onSaveConfirmedClicked() {
         val ibanInfo = _selectedIbanInfo.value

         Log.d("IbanConfirmationViewModel", "IBAN ve kullanıcı bilgileri kaydedildi")
         Log.d("IbanConfirmationViewModel", "Ülke Kodu: ${ibanInfo.countryCode}")
         Log.d("IbanConfirmationViewModel", "IBAN: ${ibanInfo.iban}")
         Log.d("IbanConfirmationViewModel", "Ad: ${ibanInfo.firstName}")
         Log.d("IbanConfirmationViewModel", "Soyad: ${ibanInfo.lastName}")
    }
}