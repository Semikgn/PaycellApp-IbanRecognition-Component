package com.example.national_iban_recognition_component.screens

import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import androidx.compose.ui.unit.toDp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController // Navigasyon için NavController'ı import ediyoruz
import androidx.navigation.compose.rememberNavController // NavController'ı Compose içinde hatırlamak için
import com.example.national_iban_recognition_component.viewmodel.IbanViewModel
import androidx.core.content.FileProvider
import kotlinx.coroutines.flow.collectLatest
import java.io.File

@Composable
fun IbanRecognitionScreen(
    navController: NavController,
    ibanViewModel: IbanViewModel = viewModel()
) {
    val context = LocalContext.current

    //ViewModel stateleri
    val selectedCountryCode by ibanViewModel.selectedCountryCode.collectAsState()
    val currentIbanText by ibanViewModel.currentIbanText.collectAsState()
    val ibanConfigs = ibanViewModel.ibanConfigs
    val photoUri by ibanViewModel.photoUri.collectAsState()
    val currentFirstName by ibanViewModel.firsName.collectAsState()
    val currentLastName by ibanViewModel.lastName.collectAsState()

    //Kamera tetikleyici
    val takePictureFullLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        ibanViewModel.onPictureTakenResult(success, context)
        // TODO: ViewModel'dan gelen Toast mesajlarını dinlemek için buraya bir yapı eklenecek.
    }
    //Kamera izni
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val photoFile = File(context.cacheDir, "temp_iban.jpg")
            val currentPhotoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            ibanViewModel.onPhotoUriCreated(currentPhotoUri)
            takePictureFullLauncher.launch(currentPhotoUri)
        } else {
            Toast.makeText(context, "Tarama için kamera izni gerekli. ", Toast.LENGTH_LONG).show()
        }
    }
    LaunchedEffect(photoUri) {
        photoUri?.let { uri ->
            Log.d(
                "IbanRecognitionScreen", "Photo Uri değişti: $uri."
            )
        }
    }
    LaunchedEffect(key1 = Unit) {
        ibanViewModel.navigateToConfirmationEvent.collectLatest { ibanInfo ->
            val route = "iban_confirmation_route/" +
                    "${ibanInfo.countryCode}/" +
                    "${ibanInfo.iban}/" +
                    "${ibanInfo.firstName}/" +
                    "${ibanInfo.lastName}"

            navController.navigate(route)
        }
    }
    //Dropdown menü için state
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                TextField(
                    value = selectedCountryCode, //Viewdan çektiğim seçili TR-GB-FR-DE
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp) // IBAN alanıyla aynı padding
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                        .onGloballyPositioned { coordinates ->
                            textFieldSize = coordinates.size.toSize()
                        }
                        .clickable { expanded = !expanded },
                    label = {Text("Ülke Seçiniz")},
                    readOnly = true,
                    trailingIcon = {
                        Icon(icon, "contentDescription",
                            Modifier.clickable { expanded = !expanded })
                    },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White
                    )
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(with(LocalDensity.current) { textFieldSize.width.toDp() }) //textfield genişliğini ayarla
                ) {
                    ibanConfigs.forEach { (code, _) ->
                        DropdownMenuItem(
                            text = { Text(code) },
                            onClick = {
                                ibanViewModel.onCountrySelected(code)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            //Tek IBAN alanı
            val currentConfig = ibanConfigs.firstOrNull { it.first == selectedCountryCode }
            val currentAccountMaxLength = currentConfig?.second ?: 0 //Seçili ülkeye göre max uzunluk
            val currentFullMaxLen = selectedCountryCode.length + currentAccountMaxLength

            IbanInputField(
                countryCode      = selectedCountryCode,
                accountMaxLength = currentAccountMaxLength,
                value             = currentIbanText, // ViewModel'dan gelen güncel IBAN
                onValueChange     = { newText ->
                    // Değişikliği ViewModel'a bildir
                    if (newText.length <= currentFullMaxLen) {
                        ibanViewModel.onIbanTextChanged(newText)
                    }
                },
                onScanClick     = {
                    // Tarama butonuna tıklandığında ViewModel'a bildir
                    ibanViewModel.onScanClicked()
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = currentFirstName,
                onValueChange = { ibanViewModel.onFirstNameChanged(it) },
                label = { Text("Adınız") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = currentLastName,
                onValueChange = { ibanViewModel.onLastNameChanged(it) },
                label = { Text("Soyadınız") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth()
            )
            //
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    // "Save" butonuna tıklandığında ViewModel'a bildir
                    ibanViewModel.onContinueClicked()
                },
                modifier = Modifier.fillMaxWidth(0.6f),
                enabled = currentIbanText.isNotBlank() &&
                        currentIbanText.length == currentFullMaxLen &&
                        currentFirstName.isNotBlank() &&
                        currentLastName.isNotBlank()

            ) {
                Text(text = "Devam Et")
            }
        }
    }
}

@Composable
fun IbanInputField(
    countryCode: String,
    accountMaxLength: Int,
    value: String,
    onValueChange: (String) -> Unit,
    onScanClick: () -> Unit,
) {
    val maxLen = countryCode.length + accountMaxLength

    TextField(
        value = value,
        onValueChange = { onValueChange(it) },
        singleLine = true,
        leadingIcon = {
            Text(
                countryCode,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 2.dp)
            )
        },
        placeholder = {
            val remaining = (maxLen - value.length).coerceAtLeast(0)
            Text("_".repeat(remaining))
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    Log.d("IbanRecognitionScreen", "Kamera açma isteği gönderildi.")
                    onScanClick()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "IBAN Tara"
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(8.dp)),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}