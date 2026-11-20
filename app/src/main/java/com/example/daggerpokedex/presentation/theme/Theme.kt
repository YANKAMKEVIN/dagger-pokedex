package com.example.daggerpokedex.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.daggerpokedex.R

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

/**
 * Rounded display face for titles, names and numbers: Fredoka (SIL Open Font License,
 * see assets/licenses/Fredoka-OFL.txt), bundled as two static weights. Body text keeps
 * the system font.
 */
val DisplayFont = FontFamily(
    Font(R.font.fredoka_semibold, FontWeight.SemiBold),
    Font(R.font.fredoka_bold, FontWeight.Bold),
)

private val Defaults = Typography()

/** One hierarchy for the three screens, instead of sizes hard-coded in each of them. */
private val PokedexTypography = Typography(
    // Splash title.
    displayMedium = Defaults.displayMedium.copy(
        fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 46.sp,
    ),
    // "Pokédex" on the list, the Pokémon's name on the detail.
    displaySmall = Defaults.displaySmall.copy(
        fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp,
    ),
    // Empty and error state titles.
    headlineSmall = Defaults.headlineSmall.copy(fontFamily = DisplayFont, fontWeight = FontWeight.Bold),
    // Detail section titles and big values ("Base stats", height, weight).
    titleLarge = Defaults.titleLarge.copy(
        fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp,
    ),
    // Card names.
    titleMedium = Defaults.titleMedium.copy(
        fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp,
    ),
    // Pokédex number on the detail header.
    titleSmall = Defaults.titleSmall.copy(
        fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp,
    ),
    // Pokédex number on the cards.
    labelMedium = Defaults.labelMedium.copy(
        fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 18.sp,
    ),
)

@Composable
fun DaggerPokedexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PokedexTypography,
        content = content,
    )
}
