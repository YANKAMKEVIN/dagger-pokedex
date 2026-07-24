package com.example.daggerpokedex.data.mapper

import com.example.daggerpokedex.data.remote.dto.PokemonDetailDto
import com.example.daggerpokedex.data.remote.dto.PokemonListItemDto
import com.example.daggerpokedex.domain.model.Pokemon
import com.example.daggerpokedex.domain.model.PokemonDetail

/**
 * Mapping functions translate network DTOs into domain models. This is the
 * boundary that keeps JSON quirks (units, nested sprite objects, ids hidden in
 * URLs) out of the rest of the app.
 */

private const val ARTWORK_BASE =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork"

/** Extracts the numeric id from a resource URL like ".../pokemon/25/". */
private fun PokemonListItemDto.extractId(): Int =
    url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: 0

fun PokemonListItemDto.toDomain(): Pokemon {
    val id = extractId()
    return Pokemon(
        id = id,
        name = name.replaceFirstChar { it.uppercase() },
        imageUrl = "$ARTWORK_BASE/$id.png",
    )
}

fun PokemonDetailDto.toDomain(): PokemonDetail = PokemonDetail(
    id = id,
    name = name.replaceFirstChar { it.uppercase() },
    // Prefer the official artwork; fall back to a computed URL if absent.
    imageUrl = sprites.other?.officialArtwork?.frontDefault ?: "$ARTWORK_BASE/$id.png",
    heightMeters = height / 10.0, // decimetres -> metres
    weightKilograms = weight / 10.0, // hectograms -> kilograms
    types = types.map { it.type.name.replaceFirstChar { c -> c.uppercase() } },
    stats = stats.map {
        PokemonDetail.Stat(
            name = it.stat.name.replace('-', ' ').replaceFirstChar { c -> c.uppercase() },
            value = it.baseStat,
        )
    },
    cryUrl = cries?.latest,
)
