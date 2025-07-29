// IbanConfirmationScreen.kt
package com.example.national_iban_recognition_component.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.national_iban_recognition_component.R
import com.example.national_iban_recognition_component.viewmodel.IbanConfirmationViewModel
import com.example.national_iban_recognition_component.utils.IbanVisualTransformation // Eğer kullanılıyorsa
import com.example.national_iban_recognition_component.model.IbanCategory // YENİ IMPORT
import androidx.compose.ui.text.AnnotatedString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IbanConfirmationScreen(
    navController: NavController,
    ibanConfirmationViewModel: IbanConfirmationViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current

    val countryCode by ibanConfirmationViewModel.countryCode.collectAsState()
    val iban by ibanConfirmationViewModel.iban.collectAsState()

    // YENİ EKLENEN STATE'LER
    val ownerFullName by ibanConfirmationViewModel.ownerFullName.collectAsState()
    val shortName by ibanConfirmationViewModel.shortName.collectAsState()
    val category by ibanConfirmationViewModel.category.collectAsState() // Enum değeri olarak geliyor

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("IBAN Bilgilerini Onayla") })
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
                text = "Lütfen bilgileri kontrol edin:",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Ülke Kodu ve Bayrak
            Row(verticalAlignment = Alignment.CenterVertically) {
                val flagResId = context.resources.getIdentifier(
                    countryCode.lowercase(), "drawable", context.packageName
                )
                if (flagResId != 0) {
                    Image(
                        painter = painterResource(id = flagResId),
                        contentDescription = "Country Flag",
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Ülke Kodu: $countryCode",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(Modifier.height(8.dp))

            // IBAN
            Text(
                text = "IBAN: ${IbanVisualTransformation(countryCode).filter(AnnotatedString(iban)).text}", // Formatlı gösterim
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))

            // YENİ ALANLARIN GÖSTERİMİ:

            // Hesap Sahibi Adı
            Text(
                text = "Hesap Sahibi: $ownerFullName",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))

            // Kısa İsim
            Text(
                text = "Kısa İsim: $shortName",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))

            // Kategori (Opsiyonel olduğu için NONE değilse göster)
            if (category != IbanCategory.NONE) {
                Text(
                    text = "Kategori: ${category.displayName}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(8.dp))
            }


            Spacer(Modifier.weight(1f)) // Alanları yukarı itmek için

            // Onayla Butonu
            Button(
                onClick = {
                    ibanConfirmationViewModel.onConfirmClicked()
                    // Buradan sonra nereye gideceğine karar verebilirsiniz,
                    // örneğin ana ekrana geri dönebilir veya bir "başarılı" ekranına yönlenebilir.
                    navController.popBackStack() // Ana ekrana geri dönme örneği
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Onayla ve Kaydet")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}