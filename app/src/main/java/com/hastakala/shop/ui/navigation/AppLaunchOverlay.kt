package com.hastakala.shop.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun AppLaunchOverlay() {
    var animateContent by remember { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(
        targetValue = if (animateContent) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "launchAlpha"
    )
    val contentScale by animateFloatAsState(
        targetValue = if (animateContent) 1f else 0.92f,
        animationSpec = tween(durationMillis = 720, easing = FastOutSlowInEasing),
        label = "launchScale"
    )

    LaunchedEffect(Unit) {
        animateContent = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .padding(28.dp)
                .scale(contentScale),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f * contentAlpha),
            shadowElevation = 16.dp,
            shape = RoundedCornerShape(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 36.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "HK",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha)
                    )
                }
                Text(
                    text = "Hasta-Kala Shop",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                )
                Text(
                    text = "Handmade inventory and selling, ready to go.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                )
            }
        }
    }
}
