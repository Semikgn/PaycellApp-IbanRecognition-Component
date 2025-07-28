package com.example.national_iban_recognition_component.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectedIbanInfo (
    val countryCode: String,
    val iban: String,
    val firstName: String,
    val lastName: String
) : Parcelable