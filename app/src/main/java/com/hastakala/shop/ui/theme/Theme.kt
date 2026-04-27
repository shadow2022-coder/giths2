package com.hastakala.shop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Clay,
    secondary = Palm,
    tertiary = Mustard,
    background = Linen,
    surface = Color.White,
    surfaceVariant = Sand,
    surfaceTint = Clay,
    outline = SoftGray.copy(alpha = 0.28f),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Ink,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = SoftGray,
    error = AlertRed
)

private val DarkColors = darkColorScheme(
    primary = Terracotta,
    secondary = Mustard,
    tertiary = Palm,
    background = Ink,
    surface = Color(0xFF171B22),
    surfaceVariant = Color(0xFF262C36),
    surfaceTint = Terracotta,
    outline = Color(0xFF3A4250),
    onPrimary = Color.White,
    onSecondary = Ink,
    onTertiary = Color.White,
    onBackground = Linen,
    onSurface = Linen,
    onSurfaceVariant = Color(0xFFB7C0CE),
    error = AlertRed
)

@Composable
fun HastaKalaShopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
