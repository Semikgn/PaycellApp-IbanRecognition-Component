// IbanRecognitionScreen.kt
package com.example.national_iban_recognition_component.screens

import android.provider.MediaStore // Bu satırı ekleyin
import android.content.ContentValues // Bu satırı ekleyin
import com.example.national_iban_recognition_component.utils.IbanVisualTransformation

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.* // Material3 paketini kullandığınızdan emin olun
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.national_iban_recognition_component.R
import com.example.national_iban_recognition_component.model.IbanCategory
import com.example.national_iban_recognition_component.viewmodel.IbanViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class) // Experimental API'ler için
@Composable
fun IbanRecognitionScreen(
    navController: NavController,
    ibanViewModel: IbanViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // CoroutineScope for sheet interactions

    // ViewModel state'leri
    val selectedCountryCode by ibanViewModel.selectedCountryCode.collectAsState()
    val currentIbanText by ibanViewModel.currentIbanText.collectAsState()
    val ibanConfigs = ibanViewModel.ibanConfigs
    val photoUri by ibanViewModel.photoUri.collectAsState()

    // OLMADAN ÖNCE:
    // val currentFirstName by ibanViewModel.firsName.collectAsState()
    // val currentLastName by ibanViewModel.lastName.collectAsState()

    // YENİ EKLENEN STATE'LER
    val ownerFullName by ibanViewModel.ownerFullName.collectAsState()
    val shortName by ibanViewModel.shortName.collectAsState()
    val selectedCategory by ibanViewModel.selectedCategory.collectAsState() // Yeni kategori state'i

    val detectedIbans by ibanViewModel.detectedIbans.collectAsState()
    val showBottomSheet by ibanViewModel.showIbanBottomSheet.collectAsState()
    val ibanError by ibanViewModel.ibanError.collectAsState()

    // BottomSheet içinde seçilen IBAN
    var selectedIbanInBottomSheet by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Toast mesajlarını dinle
    LaunchedEffect(ibanViewModel.toastMessage) {
        ibanViewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Navigasyon olaylarını dinle
    LaunchedEffect(ibanViewModel.navigateToConfirmationEvent) {
        ibanViewModel.navigateToConfirmationEvent.collect { ibanInfo ->
            navController.navigate("confirmation_screen/${ibanInfo.countryCode}/${ibanInfo.iban}/" +
                    "${ibanInfo.ownerFullName}/${ibanInfo.shortName}/${ibanInfo.category.name}") // Kategori adını iletiyoruz
        }
    }

    // Kamera ve galeri için ActivityResultLauncher'lar
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        ibanViewModel.onPictureTakenResult(success, context)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("IBAN Kaydet") })
        }
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

            // IBAN Giriş Alanı
            OutlinedTextField(
                value = currentIbanText,
                onValueChange = { ibanViewModel.onIbanTextChanged(it) },
                label = { Text("IBAN") },
                isError = ibanError != null,
                supportingText = {
                    if (ibanError != null) {
                        Text(ibanError!!)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                visualTransformation = IbanVisualTransformation(selectedCountryCode), // Eğer IBAN'ı formatlamak istiyorsanız
                leadingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, // İçerikleri dikeyde ortala
                        // Genel bir başlangıç padding'i ekleyelim, OutlinedTextField'ın varsayılan iç boşluğuna uyum sağlaması için
                        modifier = Modifier
                            .padding(start = 12.dp) // Deneyerek ayarlayabilirsiniz
                            .clickable { /* Tıklanabilirliği tüm satıra yaymak için */ } // Bu Row'u tıklanabilir yaptık
                    ) {
                        // Dropdown Menu'nün expanded state'ini burada tanımla
                        var expanded by remember { mutableStateOf(false) }

                        // Ülke bayrağını göster
                        Image(
                            painter = getFlagResId(selectedCountryCode), // getFlagResId fonksiyonunu kullanıyoruz
                            contentDescription = "Ülke Bayrağı",
                            modifier = Modifier
                                .size(24.dp) // Bayrak boyutunu ayarla. 20.dp veya 28.dp de deneyebilirsiniz.
                                .clickable { expanded = true } // Bayrağa tıklandığında da açılmasını sağlarız
                        )

                        // Bayrak ile ülke kodu metni arasında boşluk
                        Spacer(modifier = Modifier.width(8.dp)) // Bu boşluğu 4.dp veya 12.dp olarak da deneyebilirsiniz.

                        // Ülke Kodu ve Dropdown Okunu içeren kısım
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { expanded = true } // Ülke koduna veya oka tıklandığında da açılmasını sağlarız
                        ) {
                            Text(
                                text = selectedCountryCode,
                                // Eğer "TR" metninin stilini değiştirmek isterseniz buraya ekleyebilirsiniz:
                                // style = MaterialTheme.typography.bodyLarge // Örnek stil
                            )
                            // Dropdown okunu buraya ekleyelim
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Ülke Seçimi Açılır Menü",
                                modifier = Modifier
                                    .size(20.dp) // Okun boyutunu ayarlayabilirsiniz
                                    .clickable { expanded = true } // Oka tıklandığında da açılmasını sağlarız
                            )
                        }

                        // Dropdown Menüsü (Box içinde olmasına gerek yok, parent Row'un içinde düzgün çalışır)
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
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
                },
                trailingIcon = {
                    IconButton(onClick = {
                        ibanViewModel.onScanClicked()
                        val uri = context.contentResolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            android.content.ContentValues()
                        )
                        ibanViewModel.onPhotoUriCreated(uri!!)
                        takePicture.launch(uri)
                    }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Scan IBAN")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // YENİ EKLENEN UI BİLEŞENLERİ:

            // Hesap Sahibi Adı (Owner Full Name)
            OutlinedTextField(
                value = ownerFullName,
                onValueChange = { ibanViewModel.onOwnerFullNameChanged(it) },
                label = { Text("Hesap Sahibi Adı") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Kısa İsim (Short Name)
            OutlinedTextField(
                value = shortName,
                onValueChange = { ibanViewModel.onShortNameChanged(it) },
                label = { Text("Kısa İsim") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // IBAN Kategorisi (Dropdown)
            var categoryExpanded by remember { mutableStateOf(false) }
            val focusRequester = remember { FocusRequester() } // Klavye açılmasını engellemek için

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory.displayName, // Enum'un görünen adını kullan
                    onValueChange = { /* Sadece okunur, doğrudan değiştirilemez */ },
                    readOnly = true,
                    label = { Text("IBAN Kategorisi (Opsiyonel)") },
                    trailingIcon = {
                        Icon(
                            if (categoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown arrow"
                        )
                    },
                    modifier = Modifier
                        .menuAnchor() // Dropdown'u bu TextField'a bağlar
                        .fillMaxWidth()
                        .focusRequester(focusRequester) // Klavye açılmasını engeller
                )

                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    IbanCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName) },
                            onClick = {
                                ibanViewModel.onCategorySelected(category)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f)) // Alanları yukarı itmek için

            // Devam Et butonu
            Button(
                onClick = { ibanViewModel.onContinueClicked() },
                // Butonun enabled durumu
                enabled = currentIbanText.isNotBlank() &&
                        ibanError == null &&
                        ownerFullName.isNotBlank() && // Yeni zorunlu alan
                        shortName.isNotBlank(),       // Yeni zorunlu alan
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Devam Et")
            }

            Spacer(Modifier.height(16.dp))
        }

        // IBAN Listesi Bottom Sheet
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { ibanViewModel.onBottomSheetDismissed() },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Tespit Edilen IBAN'lar",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    detectedIbans.forEach { iban ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedIbanInBottomSheet = iban }
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedIbanInBottomSheet == iban),
                                onClick = { selectedIbanInBottomSheet = iban }
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
                            onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        ibanViewModel.onBottomSheetDismissed()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Vazgeç")
                        }

                        Spacer(Modifier.width(16.dp))

                        Button(
                            onClick = {
                                selectedIbanInBottomSheet?.let { iban ->
                                    val countryCode = iban.take(2).uppercase()
                                    // Bu kontrol ViewModel'da yapıldığı için burada tekrar etmeyiz
                                    // ancak UI'da bir geri bildirim gerekiyorsa burada da kontrol edilebilir.
                                    ibanViewModel.onIbanSelectedFromBottomSheet(iban)
                                    selectedIbanInBottomSheet = null // Seçimi sıfırla
                                    scope.launch { sheetState.hide() } // Bottom sheet'i gizle
                                } ?: run {
                                    scope.launch {
                                        ibanViewModel.notifyNoIbanSelected()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = selectedIbanInBottomSheet != null, // Sadece bir IBAN seçiliyse aktif ol
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Tamam")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
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