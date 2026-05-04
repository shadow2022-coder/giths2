package com.hastakala.shop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.os.LocaleListCompat
import com.hastakala.shop.ui.navigation.HastaKalaApp
import com.hastakala.shop.ui.settings.SettingsViewModel
import com.hastakala.shop.ui.theme.HastaKalaShopTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel = hiltViewModel<SettingsViewModel>()
            val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(settingsUiState.settings.languageTag) {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(settingsUiState.settings.languageTag)
                )
            }

            HastaKalaShopTheme {
                HastaKalaApp()
            }
        }
    }
}
