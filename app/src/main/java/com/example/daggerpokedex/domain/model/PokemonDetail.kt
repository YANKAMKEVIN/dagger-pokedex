package com.example.daggerpokedex.domain.model

/** Domain model for the detail screen. Still pure Kotlin, no framework types. */
data class PokemonDetail(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val heightMeters: Double,
    val weightKilograms: Double,
    val types: List<String>,
    val stats: List<Stat>,
) {
    data class Stat(val name: String, val value: Int)
}
