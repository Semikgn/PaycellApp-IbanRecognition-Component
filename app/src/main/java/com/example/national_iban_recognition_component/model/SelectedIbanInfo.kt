package com.example.national_iban_recognition_component.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class IbanCategory(val displayName: String) {
    BIREYSEL("Bireysel"),
    IS("İş"),
    KURUMSAL("Kurumsal"),
    OZEL("Özel"),
    NONE("Kategori Seç")
}

@Parcelize
data class SelectedIbanInfo (
    val countryCode: String,
    val iban: String,
    val ownerFullName: String,
    val shortName: String,
    val category: IbanCategory
) : Parcelable