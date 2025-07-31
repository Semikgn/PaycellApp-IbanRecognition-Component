// viewmodel/IbanConfirmationViewModel.kt
package com.example.national_iban_recognition_component.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.national_iban_recognition_component.model.IbanConfirmationUiState

class IbanConfirmationViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _uiState = MutableStateFlow(
        IbanConfirmationUiState(
            countryCode = savedStateHandle.get<String>("countryCode") ?: "TR",
            iban = savedStateHandle.get<String>("iban") ?: ""
        )
    )
    val uiState: StateFlow<IbanConfirmationUiState> = _uiState.asStateFlow()


    fun onConfirmClicked() {
        val confirmedIbanInfo = "IBAN: ${_uiState.value.iban}, Ülke Kodu: ${_uiState.value.countryCode}"
        println("Onaylandı: $confirmedIbanInfo")

        /*viewModelScope.launch {
            _toastMessage.emit("IBAN başarıyla kaydedildi!")
        }*/ //TODO: Buraya koyduğum uyarı ile butondaki onClick conflict

    }
}