package com.example.daggerpokedex.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PokemonDetailDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "height") val height: Int, // decimetres in the API
    @Json(name = "weight") val weight: Int, // hectograms in the API
    @Json(name = "types") val types: List<TypeSlotDto>,
    @Json(name = "stats") val stats: List<StatSlotDto>,
    @Json(name = "sprites") val sprites: SpritesDto,
    @Json(name = "cries") val cries: CriesDto?,
)

@JsonClass(generateAdapter = true)
data class CriesDto(
    // A hosted .ogg of the Pokémon's in-game cry, streamed at runtime.
    @Json(name = "latest") val latest: String?,
)

@JsonClass(generateAdapter = true)
data class TypeSlotDto(
    @Json(name = "type") val type: NamedResourceDto,
)

@JsonClass(generateAdapter = true)
data class StatSlotDto(
    @Json(name = "base_stat") val baseStat: Int,
    @Json(name = "stat") val stat: NamedResourceDto,
)

@JsonClass(generateAdapter = true)
data class NamedResourceDto(
    @Json(name = "name") val name: String,
)

@JsonClass(generateAdapter = true)
data class SpritesDto(
    @Json(name = "other") val other: OtherSpritesDto?,
)

@JsonClass(generateAdapter = true)
data class OtherSpritesDto(
    @Json(name = "official-artwork") val officialArtwork: OfficialArtworkDto?,
)

@JsonClass(generateAdapter = true)
data class OfficialArtworkDto(
    @Json(name = "front_default") val frontDefault: String?,
)
