package com.hastakala.shop.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.hastakala.shop.data.repository.PieSlice
import com.hastakala.shop.data.repository.RevenuePoint
import com.hastakala.shop.data.repository.RevenueRange
import com.hastakala.shop.ui.components.EmptyState
import com.hastakala.shop.ui.components.SectionTitle
import com.hastakala.shop.ui.components.SummaryCard
import com.hastakala.shop.ui.components.rememberTextSpeaker
import com.hastakala.shop.util.CurrencyUtils

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val textSpeaker = rememberTextSpeaker(uiState.languageTag)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionTitle(
                title = "Insights",
                subtitle = "See best sellers, income, and optional AI guidance."
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.selectedRange == RevenueRange.WEEK,
                    onClick = { viewModel.selectRange(RevenueRange.WEEK) },
                    label = { Text("This Week") }
                )
                FilterChip(
                    selected = uiState.selectedRange == RevenueRange.MONTH,
                    onClick = { viewModel.selectRange(RevenueRange.MONTH) },
                    label = { Text("This Month") }
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Week income",
                    value = CurrencyUtils.format(uiState.summary.weekRevenue),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Month income",
                    value = CurrencyUtils.format(uiState.summary.monthRevenue),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Total units sold",
                    value = uiState.summary.totalUnitsSold.toString(),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Top product",
                    value = uiState.summary.topProduct.ifBlank { "No sales yet" },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Best seller split", style = MaterialTheme.typography.titleLarge)
                    if (uiState.summary.bestSellers.isEmpty()) {
                        EmptyState(
                            title = "No chart yet",
                            body = "Once you save sales, the best-seller chart appears here."
                        )
                    } else {
                        BestSellerPieChart(uiState.summary.bestSellers)
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
                    Text("Income trend", style = MaterialTheme.typography.titleLarge)
                    if (uiState.summary.revenueTrend.isEmpty()) {
                        EmptyState(
                            title = "No income yet",
                            body = "Sales saved this week or month will show here."
                        )
                    } else {
                        RevenueBarChart(uiState.summary.revenueTrend)
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
                    Text("Optional AI help", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = if (uiState.aiConfigured) {
                            "AI runs only when you tap these buttons. Replies follow your selected language."
                        } else {
                            "Add an API key and model in Settings to enable AI actions."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            enabled = uiState.aiConfigured && !uiState.aiBusy,
                            onClick = viewModel::askDemandInsight
                        ) {
                            Text("Demand Insight")
                        }
                        Button(
                            enabled = uiState.aiConfigured && !uiState.aiBusy,
                            onClick = viewModel::askInventorySuggestion
                        ) {
                            Text("Stock Suggestion")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            enabled = textSpeaker.canSpeak && hasSpeakableInsight(uiState),
                            onClick = { textSpeaker.speak(buildSpokenInsight(uiState)) }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Listen")
                        }
                    }
                    when {
                        uiState.aiBusy -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Text("Waiting for AI…")
                            }
                        }

                        uiState.aiInsight != null -> {
                            Card {
                                Text(
                                    text = uiState.aiInsight.orEmpty(),
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }

                        else -> {
                            Text(
                                text = "Tap a button when you want AI help.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    uiState.error?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun BestSellerPieChart(slices: List<PieSlice>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PieChart(context).apply {
                    description = Description().apply { text = "" }
                    setUsePercentValues(false)
                    setDrawEntryLabels(false)
                    holeRadius = 52f
                    setHoleColor(android.graphics.Color.TRANSPARENT)
                    transparentCircleRadius = 56f
                    animateY(500)
                    legend.isEnabled = false
                }
            },
            update = { chart ->
                val entries = slices.map { PieEntry(it.value, it.label) }
                val dataSet = PieDataSet(entries, "").apply {
                    colors = listOf(
                        android.graphics.Color.parseColor("#0A84FF"),
                        android.graphics.Color.parseColor("#5AC8FA"),
                        android.graphics.Color.parseColor("#34C759"),
                        android.graphics.Color.parseColor("#FF9F0A"),
                        android.graphics.Color.parseColor("#AF52DE")
                    )
                    valueTextSize = 12f
                }
                chart.data = PieData(dataSet)
                chart.invalidate()
            }
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = Icons.Filled.PanTool,
                    contentDescription = "Handmade",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Icon(
                    imageVector = Icons.Filled.PanTool,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer(scaleX = -1f)
                )
            }
            Text(
                text = "Handmade",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RevenueBarChart(points: List<RevenuePoint>) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        factory = { context ->
            BarChart(context).apply {
                description = Description().apply { text = "" }
                axisRight.isEnabled = false
                legend.isEnabled = false
                setFitBars(true)
                animateY(500)
            }
        },
        update = { chart ->
            val entries = points.mapIndexed { index, point ->
                BarEntry(index.toFloat(), point.value)
            }
            val dataSet = BarDataSet(entries, "").apply {
                color = android.graphics.Color.parseColor("#0A84FF")
                valueTextSize = 10f
            }
            chart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(points.map { it.label })
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelRotationAngle = -25f
            }
            chart.axisLeft.axisMinimum = 0f
            chart.data = BarData(dataSet).apply { barWidth = 0.55f }
            chart.invalidate()
        }
    )
}

private fun hasSpeakableInsight(uiState: InsightsUiState): Boolean =
    uiState.aiInsight != null || uiState.summary.revenueTrend.isNotEmpty()

private fun buildSpokenInsight(uiState: InsightsUiState): String = buildString {
    uiState.aiInsight?.takeIf { it.isNotBlank() }?.let {
        append(it.toSpeechFriendly())
        append(". ")
    }
    if (uiState.summary.revenueTrend.isNotEmpty()) {
        append("Revenue by date. ")
        append(
            uiState.summary.revenueTrend.joinToString(separator = ". ") { point ->
                "${point.label}, ${CurrencyUtils.format(point.value.toDouble())}"
            }
        )
        append(".")
    }
}

private fun String.toSpeechFriendly(): String =
    replace("\n", " ")
        .replace("•", "")
        .replace("-", " ")
