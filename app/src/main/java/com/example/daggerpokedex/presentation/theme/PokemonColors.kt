package com.example.daggerpokedex.presentation.theme

import androidx.compose.ui.graphics.Color

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
