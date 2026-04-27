package com.hastakala.shop.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
) {
    data object Home : AppDestination("home", com.hastakala.shop.R.string.tab_home, Icons.Filled.Home)
    data object Sell : AppDestination("sell", com.hastakala.shop.R.string.tab_sell, Icons.Filled.ShoppingCart)
    data object Inventory : AppDestination(
        "inventory",
        com.hastakala.shop.R.string.tab_inventory,
        Icons.Filled.Inventory2
    )
    data object Insights : AppDestination(
        "insights",
        com.hastakala.shop.R.string.tab_insights,
        Icons.Filled.BarChart
    )
    data object Settings : AppDestination(
        "settings",
        com.hastakala.shop.R.string.tab_settings,
        Icons.Filled.Settings
    )

    companion object {
        val bottomTabs = listOf(Home, Sell, Inventory, Insights, Settings)
    }
}
