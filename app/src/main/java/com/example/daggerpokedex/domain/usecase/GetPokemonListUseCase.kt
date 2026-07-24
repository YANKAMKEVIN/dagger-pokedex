package com.example.daggerpokedex.domain.usecase

import com.example.daggerpokedex.domain.model.Pokemon
import com.example.daggerpokedex.domain.repository.PokemonRepository
import com.example.daggerpokedex.domain.util.Resource
import javax.inject.Inject

/**
 * A use case encapsulates ONE piece of business logic. Here it is thin (it just
 * forwards to the repository), but it is the seam where rules like paging,
 * sorting, or filtering would live — keeping the ViewModel dumb.
 *
 * ## Dagger angle: constructor injection
 * The `@Inject constructor` is the most important Dagger idiom to understand.
 * By annotating the constructor, we tell Dagger:
 *   "You know how to build a GetPokemonListUseCase — just resolve its
 *    parameters (a PokemonRepository) from the graph and call this constructor."
 * No module, no @Provides needed. Dagger writes a Factory for this class
 * automatically. The ViewModel that needs this use case just adds it as an
 * @Inject constructor parameter, and the chain resolves itself all the way down.
 */
class GetPokemonListUseCase @Inject constructor(
    private val repository: PokemonRepository,
) {
    companion object {
        const val PAGE_SIZE = 40
    }

    suspend operator fun invoke(page: Int): Resource<List<Pokemon>> {
        val offset = page * PAGE_SIZE
        return repository.getPokemonList(limit = PAGE_SIZE, offset = offset)
    }
}
