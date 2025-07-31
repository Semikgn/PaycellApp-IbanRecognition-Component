package com.example.national_iban_recognition_component.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.national_iban_recognition_component.R

@Composable
fun getFlagResId(countryCode: String): Painter {
    return painterResource(
        when (countryCode) {
            "TR" -> R.drawable.icons_flag_tr
            "GB" -> R.drawable.icons_flag_uk
            "FR" -> R.drawable.icons_flag_fr
            "DE" -> R.drawable.icons_flag_de
            else -> R.drawable.icons_flag_tr
        }
    )
}