package com.example.daggerpokedex.presentation.detail

import com.example.daggerpokedex.domain.model.PokemonDetail

/**
 * Short Pokédex-style label for a base stat ("Special attack" -> "SP. ATK"). Stat names
 * come from the mapper (PokéAPI names with spaces); an unknown one is just upper-cased.
 */
fun statLabel(name: String): String = when (name.trim().lowercase().replace('-', ' ')) {
    "hp" -> "HP"
    "attack" -> "ATK"
    "defense" -> "DEF"
    "special attack" -> "SP. ATK"
    "special defense" -> "SP. DEF"
    "speed" -> "SPD"
    else -> name.trim().uppercase()
}

/** Sum of the base stats, shown as the "Total" line. */
fun List<PokemonDetail.Stat>.total(): Int = sumOf { it.value }
