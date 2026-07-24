package com.example.daggerpokedex.domain.model

/**
 * Domain model for a single entry in the Pokémon list.
 *
 * This is a pure Kotlin class with no knowledge of JSON, Retrofit, or Android.
 * The `data` layer maps network DTOs into this shape (see [com.example.daggerpokedex.data.mapper]).
 * The rest of the app only ever sees domain models like this one.
 */
data class Pokemon(
    val id: Int,
    val name: String,
    /** Official artwork URL, derived from the id by the mapper. */
    val imageUrl: String,
)
