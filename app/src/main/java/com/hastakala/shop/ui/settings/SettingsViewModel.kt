package com.hastakala.shop.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hastakala.shop.data.repository.SettingsRepository
import com.hastakala.shop.data.repository.ShopRepository
import com.hastakala.shop.network.ai.AiManager
import com.hastakala.shop.network.ai.model.AiProvider
import com.hastakala.shop.network.ai.model.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val availableModels: List<String> = emptyList(),
    val hasStoredApiKey: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val loadingModels: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val shopRepository: ShopRepository,
    private val aiManager: AiManager
) : ViewModel() {
    private val availableModels = MutableStateFlow<List<String>>(emptyList())
    private val message = MutableStateFlow<String?>(null)
    private val error = MutableStateFlow<String?>(null)
    private val loadingModels = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settingsFlow,
        availableModels,
        message,
        error,
        loadingModels
    ) { settings, models, currentMessage, currentError, isLoading ->
        SettingsUiState(
            settings = settings,
            availableModels = models,
            hasStoredApiKey = settings.aiSettings.apiKey.isNotBlank(),
            message = currentMessage,
            error = currentError,
            loadingModels = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun saveLanguage(languageTag: String) {
        viewModelScope.launch {
            settingsRepository.saveLanguage(languageTag)
            message.value = "Language saved"
            error.value = null
        }
    }

    fun saveAiSettings(
        provider: AiProvider,
        model: String,
        apiKey: String
    ) {
        viewModelScope.launch {
            settingsRepository.saveAiSettings(provider, model, apiKey)
            message.value = "AI settings saved"
            error.value = null
        }
    }

    fun loadModels(provider: AiProvider, apiKey: String) {
        viewModelScope.launch {
            loadingModels.value = true
            aiManager.listModels(
                settings = uiState.value.settings.aiSettings.copy(
                    provider = provider,
                    baseUrl = provider.defaultBaseUrl,
                    apiKey = apiKey
                )
            ).onSuccess {
                availableModels.value = it
                message.value = "Models loaded"
                error.value = null
            }.onFailure {
                error.value = it.message ?: "Could not load models."
            }
            loadingModels.value = false
        }
    }

    fun clearSavedApiKey() {
        viewModelScope.launch {
            settingsRepository.clearAiApiKey()
            message.value = "Saved API key removed"
            error.value = null
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            shopRepository.exportSalesCsv()
                .onSuccess {
                    message.value = "CSV saved: ${it.absolutePath}"
                    error.value = null
                }
                .onFailure {
                    error.value = it.message ?: "CSV export failed."
                }
        }
    }

    fun backupJson() {
        viewModelScope.launch {
            shopRepository.backupToJson()
                .onSuccess {
                    message.value = "JSON backup saved: ${it.absolutePath}"
                    error.value = null
                }
                .onFailure {
                    error.value = it.message ?: "JSON backup failed."
                }
        }
    }

    fun clearFeedback() {
        message.value = null
        error.value = null
    }
}
