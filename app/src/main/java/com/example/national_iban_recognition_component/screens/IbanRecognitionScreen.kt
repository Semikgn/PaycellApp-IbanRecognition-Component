// IbanRecognitionScreen.kt
package com.example.national_iban_recognition_component.screens

import android.content.ContentValues
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.national_iban_recognition_component.R
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.national_iban_recognition_component.utils.getFlagResId
import com.example.national_iban_recognition_component.viewmodel.IbanViewModel

import com.example.national_iban_recognition_component.components.IbanInputField //IbanInputField Component
import com.example.national_iban_recognition_component.components.CustomBottomSheet //BottomSheet Component
import com.example.national_iban_recognition_component.components.IbanListContent //IbanListComponent
import com.example.national_iban_recognition_component.components.CustomButton //CustomButton Component
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IbanRecognitionScreen(
    navController: NavController,
    ibanViewModel: IbanViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val uiState by ibanViewModel.uiState.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            ibanViewModel.onPictureTakenResult(success, context)
        }
    }

    LaunchedEffect(ibanViewModel.navigateToConfirmationEvent) {
        ibanViewModel.navigateToConfirmationEvent.collect { event ->
            navController.navigate("confirmation_screen/${event.countryCode}/${event.iban}")
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("IBAN Kaydet") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
//Iban Input Field Component
            ibanViewModel.ibanConfigs.forEach { (countryCode, _) ->
                val ibanState = uiState.ibanInputStates[countryCode] ?: return@forEach
                IbanInputField(
                    ibanText = ibanState.text,
                    onIbanTextChanged = { newText ->
                        ibanViewModel.onIbanTextChanged(countryCode, newText)
                    },
                    countryCode = countryCode,
                    label = countryCode,
                    isError = ibanState.error != null,
                    supportingText = ibanState.error,
                    onCameraClick = {
                        ibanViewModel.onScanClicked(countryCode)
                        val uri = context.contentResolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            ContentValues()
                        )
                        ibanViewModel.onPhotoUriCreated(uri!!)
                        takePicture.launch(uri)
                    },
                    flagPainter = getFlagResId(countryCode),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.weight(1f))
//Button Component
            CustomButton(
                text = stringResource(id = R.string.continue_button),
                onClick = { ibanViewModel.onContinueClicked() },
                isEnabled = uiState.ibanInputStates.values.any { it.text.isNotBlank() && it.error == null }
            )

            Spacer(Modifier.height(16.dp))
        }
//BottomSheet Component
        if (uiState.showIbanBottomSheet) {
            CustomBottomSheet(
                isVisible = uiState.showIbanBottomSheet,
                onDismissRequest = {
                    ibanViewModel.onBottomSheetDismissed()
                }
            ) {
                IbanListContent(
                    detectedIbans = uiState.detectedIbans,
                    selectedIban = uiState.selectedIbanInBottomSheet,
                    onIbanClicked = { iban ->
                        ibanViewModel.onBottomSheetIbanSelected(iban)
                    },
                    onDismissRequest = {
                        ibanViewModel.onBottomSheetDismissed()
                    },
                    onConfirmClick = {
                        uiState.selectedIbanInBottomSheet?.let { iban ->
                            ibanViewModel.onIbanSelectedFromBottomSheet(iban)
                            ibanViewModel.onBottomSheetDismissed()
                        }
                    }
                )
            }
        }
    }
}
