package com.hastakala.shop.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hastakala.shop.network.ai.model.AiProvider
import com.hastakala.shop.network.ai.model.ReplyLanguage
import com.hastakala.shop.ui.components.SectionTitle
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedProvider by rememberSaveable { mutableStateOf(uiState.settings.aiSettings.provider) }
    var selectedLanguage by rememberSaveable { mutableStateOf(uiState.settings.languageTag) }
    var model by rememberSaveable { mutableStateOf(uiState.settings.aiSettings.model) }
    var apiKey by remember { mutableStateOf("") }

    LaunchedEffect(uiState.settings) {
        selectedProvider = uiState.settings.aiSettings.provider
        selectedLanguage = uiState.settings.languageTag
        model = uiState.settings.aiSettings.model
        apiKey = ""
    }

    LaunchedEffect(uiState.message, uiState.error) {
        val text = uiState.message ?: uiState.error
        if (!text.isNullOrBlank()) {
            snackbarHostState.showSnackbar(text)
            viewModel.clearFeedback()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionTitle(
                    title = "Settings",
                    subtitle = "The app works fully offline. AI is optional."
                )
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Language", style = MaterialTheme.typography.titleLarge)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReplyLanguage.entries.forEach { language ->
                                FilterChip(
                                    selected = selectedLanguage == language.tag,
                                    onClick = {
                                        selectedLanguage = language.tag
                                        viewModel.saveLanguage(language.tag)
                                    },
                                    label = { Text(language.label) }
                                )
                            }
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("AI provider", style = MaterialTheme.typography.titleLarge)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AiProvider.entries.forEach { provider ->
                                FilterChip(
                                    selected = selectedProvider == provider,
                                    onClick = { selectedProvider = provider },
                                    label = { Text(provider.displayName) }
                                )
                            }
                        }
                        Text(
                            text = "Official endpoint: ${selectedProvider.defaultBaseUrl}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = {
                                Text(
                                    if (uiState.hasStoredApiKey) {
                                        "Replace API key"
                                    } else {
                                        "API key"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true
                        )
                        if (uiState.hasStoredApiKey && apiKey.isBlank()) {
                            Text(
                                text = "A key is already saved securely on this device and is hidden here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedTextField(
                            value = model,
                            onValueChange = { model = it },
                            label = { Text("Model") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                viewModel.loadModels(
                                    provider = selectedProvider,
                                    apiKey = apiKey.ifBlank { uiState.settings.aiSettings.apiKey }
                                )
                            }
                        ) {
                            Text(if (uiState.loadingModels) "Loading…" else "Fetch Models")
                        }
                        if (uiState.availableModels.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.availableModels.forEach { availableModel ->
                                    FilterChip(
                                        selected = model == availableModel,
                                        onClick = { model = availableModel },
                                        label = { Text(availableModel) }
                                    )
                                }
                            }
                        }
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                viewModel.saveAiSettings(
                                    provider = selectedProvider,
                                    model = model,
                                    apiKey = apiKey.ifBlank { uiState.settings.aiSettings.apiKey }
                                )
                            }
                        ) {
                            Text("Save AI Settings")
                        }
                        if (uiState.hasStoredApiKey) {
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = viewModel::clearSavedApiKey
                            ) {
                                Text("Clear Saved API Key")
                            }
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Data management", style = MaterialTheme.typography.titleLarge)
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = viewModel::exportCsv
                        ) {
                            Text("Export CSV")
                        }
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = viewModel::backupJson
                        ) {
                            Text("Backup JSON")
                        }
                    }
                }
            }
        }
    }
}
