//components/IbanInputField.kt
package com.example.national_iban_recognition_component.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.national_iban_recognition_component.utils.IbanVisualTransformation
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.ImeAction

@Composable
fun IbanInputField(
    ibanText: String,
    onIbanTextChanged: (String) -> Unit,
    countryCode: String,
    label: String,
    isError: Boolean,
    supportingText: String?,
    onCameraClick: () -> Unit,
    flagPainter: Painter,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = ibanText,
        onValueChange = onIbanTextChanged,
        label = { Text(label) },
        isError = isError,
        supportingText = {
            if (supportingText != null) {
                Text(supportingText)
            }
        },
        keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done
        ),
        leadingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Image(
                    painter = flagPainter,
                    contentDescription = "Ülke Bayrağı",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        },
        trailingIcon = {
            IconButton(onClick = onCameraClick) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Scan IBAN")
            }
        },
        modifier = modifier,
        visualTransformation = IbanVisualTransformation(countryCode),
        singleLine = true
    )
}