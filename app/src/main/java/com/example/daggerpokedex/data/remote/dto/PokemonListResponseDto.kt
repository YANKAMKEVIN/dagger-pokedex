package com.example.daggerpokedex.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTOs (Data Transfer Objects) mirror the JSON shape returned by the PokéAPI
 * *exactly*. They live in the data layer and never escape it — the mapper turns
 * them into clean domain models. Keeping DTOs separate means a change in the API
 * response never ripples into the UI.
 *
 * `@JsonClass(generateAdapter = true)` asks Moshi's codegen to create a fast,
 * reflection-free adapter for this class.
 */
@JsonClass(generateAdapter = true)
data class PokemonListResponseDto(
    @Json(name = "count") val count: Int,
    @Json(name = "results") val results: List<PokemonListItemDto>,
)

@JsonClass(generateAdapter = true)
data class PokemonListItemDto(
    @Json(name = "name") val name: String,
    // The list endpoint only gives a URL like ".../pokemon/25/"; the numeric id
    // is embedded in it. The mapper extracts it to build the artwork URL.
    @Json(name = "url") val url: String,
)
