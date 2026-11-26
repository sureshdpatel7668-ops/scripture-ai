package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimaryDark,
    secondary = SlateSecondaryDark,
    tertiary = RadiantGoldAccent,
    background = DarkCharcoalBg,
    surface = DarkSlateSurface,
    onPrimary = DarkCharcoalBg,
    onSecondary = DarkCharcoalBg,
    onTertiary = DarkCharcoalBg,
    onBackground = AlabasterBg,
    onSurface = AlabasterBg
)

private val LightColorScheme = lightColorScheme(
    primary = BronzePrimary,
    secondary = SlateSecondary,
    tertiary = WarmAccent,
    background = AlabasterBg,
    surface = SepiaSurface,
    onPrimary = AlabasterBg,
    onSecondary = AlabasterBg,
    onTertiary = AlabasterBg,
    onBackground = DarkCharcoalBg,
    onSurface = DarkCharcoalBg
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
