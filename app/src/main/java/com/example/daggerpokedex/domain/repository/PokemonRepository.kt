package com.example.daggerpokedex.domain.repository

import com.example.daggerpokedex.domain.model.Pokemon
import com.example.daggerpokedex.domain.model.PokemonDetail
import com.example.daggerpokedex.domain.util.Resource

/**
 * The repository CONTRACT lives in the domain layer as an interface.
 *
 * This is the key inversion of Clean Architecture: the domain declares *what* it
 * needs, and the `data` layer provides the *how*. Use cases depend on this
 * interface, never on the concrete Retrofit-backed implementation.
 *
 * Dagger is what connects the two at runtime: in the DI graph we tell Dagger
 * "whenever something asks for a [PokemonRepository], give it a
 * `PokemonRepositoryImpl`" (see RepositoryModule with @Binds).
 */
interface PokemonRepository {
    suspend fun getPokemonList(limit: Int, offset: Int): Resource<List<Pokemon>>
    suspend fun getPokemonDetail(name: String): Resource<PokemonDetail>
}
