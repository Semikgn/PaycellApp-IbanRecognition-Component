// components/IbanListContent.kt
package com.example.national_iban_recognition_component.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.national_iban_recognition_component.R
import androidx.compose.ui.res.stringResource


@Composable
fun IbanListContent(
    detectedIbans: List<String>,
    selectedIban: String?,
    onIbanClicked: (String) -> Unit,
    onDismissRequest: () -> Unit,  // Vazgec
    onConfirmClick: () -> Unit  // Tamam
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.detected_ibans_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        detectedIbans.forEach { iban ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIbanClicked(iban) }
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedIban == iban),
                    onClick = { onIbanClicked(iban) }
                )
                Spacer(Modifier.width(8.dp))
                Text(iban)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = onDismissRequest,
                modifier = Modifier.weight(1f),
            ) {
                Text("Vazgeç")
            }

            Spacer(Modifier.width(16.dp))

            Button(
                onClick = onConfirmClick,
                modifier = Modifier.weight(1f),
                enabled = selectedIban != null,
            ) {
                Text("Tamam")
            }
        }
    }
}