// IbanConfirmationScreen.kt
package com.example.national_iban_recognition_component.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.national_iban_recognition_component.R
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.national_iban_recognition_component.viewmodel.IbanConfirmationViewModel
import com.example.national_iban_recognition_component.components.CustomButton //Button Component
import com.example.national_iban_recognition_component.components.CustomInfoBox //Info Box Component

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IbanConfirmationScreen(
    navController: NavController,
    ibanConfirmationViewModel: IbanConfirmationViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val uiState by ibanConfirmationViewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.iban_confirm_title)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.iban_recognition_screen_info),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
//Info Box Component
            CustomInfoBox(
                title = stringResource(id = R.string.iban_label),
                value = AnnotatedString(uiState.iban).text.toString()
            )

            Spacer(Modifier.weight(1f))

//Custom Button Component
            CustomButton(
                text = stringResource(id = R.string.confirm_and_save_button),
                onClick = {
                    ibanConfirmationViewModel.onConfirmClicked()
                    navController.popBackStack()
                }
            )
        }
    }
}