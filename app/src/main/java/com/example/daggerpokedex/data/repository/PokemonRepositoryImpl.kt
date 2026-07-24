package com.example.daggerpokedex.data.repository

import com.example.daggerpokedex.data.mapper.toDomain
import com.example.daggerpokedex.data.remote.PokeApiService
import com.example.daggerpokedex.domain.model.Pokemon
import com.example.daggerpokedex.domain.model.PokemonDetail
import com.example.daggerpokedex.di.qualifier.IoDispatcher
import com.example.daggerpokedex.domain.repository.PokemonRepository
import com.example.daggerpokedex.domain.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import retrofit2.HttpException

/**
 * The concrete repository. It calls Retrofit, maps DTOs to domain models, and
 * converts thrown exceptions into a [Resource] so the UI never has to catch
 * network exceptions itself.
 *
 * ## Dagger angle
 * - `@Inject constructor` again: Dagger can build this class as long as it can
 *   supply a [PokeApiService] (provided by NetworkModule) and a
 *   [CoroutineDispatcher] (provided by AppModule, qualified with @IoDispatcher).
 * - This class is bound to the [PokemonRepository] interface with `@Binds` in
 *   RepositoryModule, so callers depend on the abstraction, not on this type.
 */
class PokemonRepositoryImpl @Inject constructor(
    private val api: PokeApiService,
    // The @IoDispatcher qualifier tells Dagger WHICH CoroutineDispatcher binding
    // to use — matching the qualified provider in AppModule.
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PokemonRepository {

    override suspend fun getPokemonList(limit: Int, offset: Int): Resource<List<Pokemon>> =
        safeCall {
            api.getPokemonList(limit = limit, offset = offset)
                .results
                .map { it.toDomain() }
        }

    override suspend fun getPokemonDetail(name: String): Resource<PokemonDetail> =
        safeCall {
            api.getPokemonDetail(name.lowercase()).toDomain()
        }

    /** Runs [block] on the IO dispatcher and normalises errors into [Resource]. */
    private suspend fun <T> safeCall(block: suspend () -> T): Resource<T> =
        withContext(ioDispatcher) {
            try {
                Resource.Success(block())
            } catch (e: IOException) {
                Resource.Error("No internet connection. Please check your network.", e)
            } catch (e: HttpException) {
                Resource.Error("Server error (${e.code()}). Please try again.", e)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Something went wrong.", e)
            }
        }
}
