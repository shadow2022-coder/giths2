package com.hastakala.shop.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hastakala.shop.ui.home.HomeScreen
import com.hastakala.shop.ui.inventory.InventoryScreen
import com.hastakala.shop.ui.insights.InsightsScreen
import com.hastakala.shop.ui.sell.SellScreen
import com.hastakala.shop.ui.settings.SettingsScreen
import kotlinx.coroutines.delay

@Composable
fun HastaKalaApp() {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route
    var showLaunchOverlay by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1450)
        showLaunchOverlay = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    tonalElevation = 0.dp
                ) {
                    AppDestination.bottomTabs.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            icon = { androidx.compose.material3.Icon(destination.icon, null) },
                            label = { Text(stringResource(destination.labelRes)) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = AppDestination.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(AppDestination.Home.route) { HomeScreen() }
                composable(AppDestination.Sell.route) { SellScreen() }
                composable(AppDestination.Inventory.route) { InventoryScreen() }
                composable(AppDestination.Insights.route) { InsightsScreen() }
                composable(AppDestination.Settings.route) { SettingsScreen() }
            }
        }

        AnimatedVisibility(
            visible = showLaunchOverlay,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AppLaunchOverlay()
        }
    }
}
