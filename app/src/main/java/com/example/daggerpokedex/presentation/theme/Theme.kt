package com.example.daggerpokedex.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Crimson = Color(0xFFE3350D) // Poké-red accent
private val Midnight = Color(0xFF1B1B2F)

private val LightColors = lightColorScheme(
    primary = Crimson,
    onPrimary = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Crimson,
    onPrimary = Color.White,
    background = Midnight,
    surface = Color(0xFF25253D),
)

@Composable
fun DaggerPokedexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
