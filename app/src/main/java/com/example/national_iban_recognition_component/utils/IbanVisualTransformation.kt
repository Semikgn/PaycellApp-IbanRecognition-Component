// com.example.national_iban_recognition_component.utils/IbanVisualTransformation.kt
package com.example.national_iban_recognition_component.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class IbanVisualTransformation(private val countryCode: String) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val formattedText = formatIban(originalText, countryCode)

        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var transformedOffset = offset
                var spacesAdded = 0
                // Hata almamak için formattedText.length'i kontrol etmeliyiz
                for (i in 0 until offset) {
                    // i + spacesAdded'ın formattedText'in sınırları içinde olup olmadığını kontrol et
                    if (i + spacesAdded < formattedText.length && formattedText[i + spacesAdded] == ' ') {
                        spacesAdded++
                    }
                }
                return (offset + spacesAdded).coerceAtMost(formattedText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                var originalOffset = offset
                var spacesRemoved = 0
                // Hata almamak için formattedText.length'i kontrol etmeliyiz
                for (i in 0 until offset) {
                    // i'nin formattedText'in sınırları içinde olup olmadığını kontrol et
                    if (i < formattedText.length && formattedText[i] == ' ') {
                        spacesRemoved++
                    }
                }
                return (offset - spacesRemoved).coerceAtMost(originalText.length)
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetTranslator)
    }

    private fun formatIban(iban: String, countryCode: String): String {
        return when (countryCode.uppercase()) {
            "TR" -> formatByGroups(iban, 4, 4, 4, 4, 4, 4)
            "DE" -> formatByGroups(iban, 4, 4, 4, 4, 4)
            "GB" -> formatByGroups(iban, 4, 4, 4, 4, 2)
            "FR" -> formatByGroups(iban, 4, 4, 4, 4, 4, 3)
            else -> iban
        }
    }

    private fun formatByGroups(iban: String, vararg groupSizes: Int): String {
        val result = StringBuilder()
        var currentOffset = 0
        for (size in groupSizes) {
            if (currentOffset >= iban.length) break
            val endOffset = (currentOffset + size).coerceAtMost(iban.length)
            result.append(iban.substring(currentOffset, endOffset))
            currentOffset = endOffset
            if (currentOffset < iban.length) {
                result.append(" ")
            }
        }
        return result.toString().trim()
    }
}