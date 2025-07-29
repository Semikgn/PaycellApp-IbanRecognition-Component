package com.example.national_iban_recognition_component

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.national_iban_recognition_component.screens.IbanRecognitionScreen
import com.example.national_iban_recognition_component.screens.IbanConfirmationScreen
import com.example.national_iban_recognition_component.ui.theme.NationalIbanRecognitionComponentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NationalIbanRecognitionComponentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "iban_recognition_route") {

                        // IBAN Tanıma Ekranı rotası
                        composable("iban_recognition_route") {
                            IbanRecognitionScreen(navController = navController)
                        }

                        // IBAN Onay Ekranı rotası
                        composable(
                            "confirmation_screen/{countryCode}/{iban}/{ownerFullName}/{shortName}/{category}",
                            arguments = listOf(
                                navArgument("countryCode") { type = NavType.StringType },
                                navArgument("iban") { type = NavType.StringType },
                                navArgument("ownerFullName") { type = NavType.StringType }, // Yeni argüman
                                navArgument("shortName") { type = NavType.StringType },     // Yeni argüman
                                navArgument("category") { type = NavType.StringType }      // Yeni argüman (Enum adı olarak)
                            )
                        ) { backStackEntry ->
                            IbanConfirmationScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}