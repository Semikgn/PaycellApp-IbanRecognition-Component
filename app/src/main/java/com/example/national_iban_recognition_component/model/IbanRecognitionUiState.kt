// model/IbanRecognitionUiState.kt
package com.example.national_iban_recognition_component.model

import com.example.national_iban_recognition_component.viewmodel.IbanInputState

data class IbanRecognitionUiState(
    val ibanInputStates: Map<String, IbanInputState> = emptyMap(),
    val detectedIbans: List<String> = emptyList(),
    val showIbanBottomSheet: Boolean = false,
    val selectedIbanInBottomSheet: String? = null,
    val toastMessage: String? = null
)