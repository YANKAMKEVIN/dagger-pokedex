package com.example.daggerpokedex.data.remote

import com.example.daggerpokedex.data.remote.dto.PokemonDetailDto
import com.example.daggerpokedex.data.remote.dto.PokemonListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * The Retrofit interface describing the PokéAPI endpoints we use.
 *
 * We never `new` this ourselves — Retrofit generates the implementation, and the
 * NetworkModule teaches Dagger how to hand that implementation out via
 * `@Provides`. That is exactly the case Dagger's modules exist for: providing a
 * type whose construction we do not control.
 */
interface PokeApiService {

    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): PokemonListResponseDto

    @GET("pokemon/{name}")
    suspend fun getPokemonDetail(
        @Path("name") name: String,
    ): PokemonDetailDto
}
