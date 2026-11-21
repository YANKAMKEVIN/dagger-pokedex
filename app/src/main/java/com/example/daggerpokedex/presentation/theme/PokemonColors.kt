package com.example.daggerpokedex.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/**
 * Canonical Pokémon **type** colors, used to tint the detail header and stat bars.
 * Keys are lowercase type names as returned by the PokéAPI.
 */
private val typeColors: Map<String, Color> = mapOf(
    "normal" to Color(0xFFA8A77A),
    "fire" to Color(0xFFEE8130),
    "water" to Color(0xFF6390F0),
    "electric" to Color(0xFFF7D02C),
    "grass" to Color(0xFF7AC74C),
    "ice" to Color(0xFF96D9D6),
    "fighting" to Color(0xFFC22E28),
    "poison" to Color(0xFFA33EA1),
    "ground" to Color(0xFFE2BF65),
    "flying" to Color(0xFFA98FF3),
    "psychic" to Color(0xFFF95587),
    "bug" to Color(0xFFA6B91A),
    "rock" to Color(0xFFB6A136),
    "ghost" to Color(0xFF735797),
    "dragon" to Color(0xFF6F35FC),
    "dark" to Color(0xFF705746),
    "steel" to Color(0xFFB7B7CE),
    "fairy" to Color(0xFFD685AD),
)

/** Brand red used for the splash and primary accents (Poké Ball red). */
val PokeRed = Color(0xFFDC0A2D)

/** Dark text for light accents (same navy as the dark theme background). */
val DarkInk = Color(0xFF1B1B2F)

/**
 * Text and icon color that stays readable on [background]: dark ink on light colors
 * (electric yellow, ice cyan, ground sand), white on the others.
 */
fun contentColorOn(background: Color): Color =
    if (background.luminance() > 0.5f) DarkInk else Color.White

/** WCAG contrast ratio between two colors, from 1 (same luminance) to 21 (black on white). */
fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
}

/**
 * [accent] as a text color on [background]: unchanged when it is already readable (WCAG AA,
 * [minContrast] 4.5), otherwise darkened (light background) or lightened (dark background) just
 * enough. Electric yellow or ice cyan text on the white detail page becomes a deep gold or teal
 * of the same hue; bars and fills keep the true type color.
 */
fun readableAccentOn(accent: Color, background: Color, minContrast: Float = 4.5f): Color {
    if (contrastRatio(accent, background) >= minContrast) return accent
    val toward = if (background.luminance() > 0.5f) Color.Black else Color.White
    for (step in 1..20) {
        val candidate = lerp(accent, toward, step / 20f)
        if (contrastRatio(candidate, background) >= minContrast) return candidate
    }
    return toward
}

/** Returns the color for a type name (case-insensitive), or a neutral fallback. */
fun colorForType(type: String): Color =
    typeColors[type.lowercase()] ?: Color(0xFFA8A77A)

/**
 * A stable, vibrant background color for a list card. The list endpoint does not
 * return types, so we cycle a curated palette by id to get the colorful grid look
 * seen in popular Pokédex designs — deterministic, so a card keeps its color.
 */
private val cardPalette: List<Color> = listOf(
    Color(0xFF7AC74C), // grass green
    Color(0xFFEE8130), // fire orange
    Color(0xFF6390F0), // water blue
    Color(0xFFF7D02C), // electric yellow
    Color(0xFFF95587), // psychic pink
    Color(0xFF6F35FC), // dragon purple
    Color(0xFF96D9D6), // ice cyan
    Color(0xFFA6B91A), // bug lime
    Color(0xFFD685AD), // fairy rose
    Color(0xFFB6A136), // rock gold
)

fun cardColorForId(id: Int): Color = cardPalette[(id.coerceAtLeast(0)) % cardPalette.size]
