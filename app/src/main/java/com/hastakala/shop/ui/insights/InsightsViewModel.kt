package com.hastakala.shop.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hastakala.shop.data.repository.InsightsSummary
import com.hastakala.shop.data.repository.RevenueRange
import com.hastakala.shop.data.repository.SettingsRepository
import com.hastakala.shop.data.repository.ShopRepository
import com.hastakala.shop.network.ai.AiManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InsightsUiState(
    val selectedRange: RevenueRange = RevenueRange.WEEK,
    val summary: InsightsSummary = InsightsSummary(),
    val aiConfigured: Boolean = false,
    val aiBusy: Boolean = false,
    val aiInsight: String? = null,
    val languageTag: String = "en",
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    private val settingsRepository: SettingsRepository,
    private val aiManager: AiManager
) : ViewModel() {
    private val selectedRange = MutableStateFlow(RevenueRange.WEEK)
    private val aiBusy = MutableStateFlow(false)
    private val aiInsight = MutableStateFlow<String?>(null)
    private val error = MutableStateFlow<String?>(null)
    private val inventorySnapshot = shopRepository.observeInventory().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val coreState = combine(
        selectedRange,
        selectedRange.flatMapLatest { shopRepository.observeInsights(it) },
        settingsRepository.settingsFlow,
        aiBusy,
        aiInsight
    ) { range, summary, settings, busy, insight ->
        InsightsUiState(
            selectedRange = range,
            summary = summary,
            aiConfigured = settings.aiSettings.apiKey.isNotBlank() && settings.aiSettings.model.isNotBlank(),
            aiBusy = busy,
            aiInsight = insight,
            languageTag = settings.languageTag
        )
    }

    val uiState: StateFlow<InsightsUiState> = combine(coreState, error) { baseState, currentError ->
        baseState.copy(error = currentError)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InsightsUiState()
    )

    fun selectRange(range: RevenueRange) {
        selectedRange.value = range
    }

    fun askDemandInsight() {
        viewModelScope.launch {
            aiBusy.value = true
            val settings = settingsRepository.currentSettings().aiSettings
            aiManager.demandInsight(
                settings = settings,
                bestSellers = uiState.value.summary.bestSellers
            ).onSuccess {
                aiInsight.value = it
                error.value = null
            }.onFailure {
                error.value = it.message ?: "AI insight failed."
            }
            aiBusy.value = false
        }
    }

    fun askInventorySuggestion() {
        viewModelScope.launch {
            aiBusy.value = true
            val settings = settingsRepository.currentSettings().aiSettings
            aiManager.inventorySuggestion(
                settings = settings,
                inventoryItems = inventorySnapshot.value
            ).onSuccess {
                aiInsight.value = it
                error.value = null
            }.onFailure {
                error.value = it.message ?: "AI suggestion failed."
            }
            aiBusy.value = false
        }
    }

    fun clearFeedback() {
        error.value = null
    }
}
