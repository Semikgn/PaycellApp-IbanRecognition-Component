package com.example.national_iban_recognition_component.screens

import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.content.MediaType.Companion.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.painter.Painter
import com.example.national_iban_recognition_component.R
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IbanRecognitionScreen(
    navController: NavController,
    ibanViewModel: IbanViewModel = viewModel()
) {
    val context = LocalContext.current

    //ViewModel stateleri
    val selectedCountryCode by ibanViewModel.selectedCountryCode.collectAsState()
    val currentIbanText by ibanViewModel.currentIbanText.collectAsState()
    val ibanConfigs = ibanViewModel.ibanConfigs // ibabConfigs'in buradan gelmesi doğru
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

    // Dropdown menü için state (mevcut değişkenleri koruyorum)
    var expanded by remember { mutableStateOf(false) } // Bu değişkeni kullanacağız
    // var textFieldSize by remember { mutableStateOf(Size.Zero) } // Bu değişkene artık ihtiyacımız yok
    // val icon = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown // Bu ikona da doğrudan ihtiyacımız yok

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val currentConfig = ibanConfigs.firstOrNull { it.first == selectedCountryCode }
            val currentAccountMaxLength = currentConfig?.second ?: 0
            val currentFullMaxLen = selectedCountryCode.length + currentAccountMaxLength

            Text(
                text = "IBAN Kaydet",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            OutlinedTextField(
                placeholder = {
                    val remaining = (currentFullMaxLen - currentIbanText.length).coerceAtLeast(0)
                    Text("_".repeat(remaining))
                },
                value = currentIbanText, // ViewModel'dan gelen güncel IBAN
                onValueChange = { newText ->
                    if (newText.length <= currentFullMaxLen) {
                        ibanViewModel.onIbanTextChanged(newText)
                    }
                },
                label = { Text("IBAN") },
                //TODO: Burada isError yorumda kaldı buraya bir uyarı mesajı ekleyeceğim
                // isError = errorMessageForSelectedCountry.isNotBlank(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                leadingIcon = {
                    Row(
                        modifier = Modifier
                            .clickable { expanded = !expanded } // Tıkla Dropdown aç
                            .padding(start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ülke koduna göre bayrak getir
                        Image(
                            painter = getFlagResId(selectedCountryCode),
                            contentDescription = "Flag for $selectedCountryCode",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        Text(text = selectedCountryCode, fontWeight = FontWeight.Bold)
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Select Country"
                        )
                        // DropdownMenu'yu doğrudan burada tanımlıyoruz
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            ibanConfigs.forEach { (code, _) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Image(
                                                painter = getFlagResId(code),
                                                contentDescription = "Flag for $code",
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = code)
                                        }
                                    },
                                    onClick = {
                                        ibanViewModel.onCountrySelected(code)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                trailingIcon = { // Sağdaki kamera ikonu
                    IconButton(onClick = {
                        ibanViewModel.onScanClicked() // ViewModel'deki onScanClicked'ı çağır
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "IBAN Tara")
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Black,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            // Diğer OutlinedTextField'larınız ve Button aynı kalacak
            OutlinedTextField(
                value = currentFirstName,
                onValueChange = { ibanViewModel.onFirstNameChanged(it) },
                label = { Text("Adınız") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = currentLastName,
                onValueChange = { ibanViewModel.onLastNameChanged(it) },
                label = { Text("Soyadınız") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
            )
            //
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
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