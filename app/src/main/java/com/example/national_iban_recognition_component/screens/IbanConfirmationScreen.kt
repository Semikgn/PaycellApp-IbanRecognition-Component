package com.example.national_iban_recognition_component.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.national_iban_recognition_component.viewmodel.IbanConfirmationViewModel

@Composable
fun IbanConfirmationScreen(
    navController: NavController,
    ibanConfirmationViewModel: IbanConfirmationViewModel = viewModel()
) {
    val selectedIbanInfo by ibanConfirmationViewModel.selectedIbanInfo.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "IBAN ve Kullanıcı Bilgileri",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Text(
            text = "Ülke Kodu: ${selectedIbanInfo.countryCode}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "IBAN: ${selectedIbanInfo.iban}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Adınız: ${selectedIbanInfo.firstName}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Soyadınız: ${selectedIbanInfo.lastName}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                ibanConfirmationViewModel.onSaveConfirmedClicked()
                //TODO: Kaydet dedikten sonra ibanlarım benzeri bir menü yapabilirim daha sonra
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(text = "Kaydet ve Onayla")
        }
    }
}