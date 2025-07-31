// model/IbanConfirmationUiState.kt
package com.example.national_iban_recognition_component.model

data class IbanConfirmationUiState(
    val countryCode: String = "",
    val iban: String = "",
    val toastMessage: String? = null
)