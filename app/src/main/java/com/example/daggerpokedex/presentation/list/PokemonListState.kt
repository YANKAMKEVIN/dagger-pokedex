package com.example.daggerpokedex.presentation.list

import com.example.daggerpokedex.domain.model.Pokemon

/**
 * The single immutable snapshot of everything the list screen needs to render.
 * MVVM: the ViewModel owns this state and the Composable is a pure function of it.
 *
 * Two distinct loading flags support infinite scroll:
 *  - [isLoading]     -> the very first page (full-screen spinner).
 *  - [isLoadingMore] -> appending a further page (small footer spinner).
 * [endReached] flips to true once the API returns a short page, so we stop asking.
 * [errorMessage] is a failure of the first page (full-screen error); [loadMoreError] a failure of
 * a further page (footer with a retry button, the loaded Pokémon stay on screen).
 * [lookup] is the search query looked up in the whole Pokédex, when none of the loaded Pokémon match.
 */
data class PokemonListState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val pokemons: List<Pokemon> = emptyList(),
    val errorMessage: String? = null,
    val endReached: Boolean = false,
    val loadMoreError: String? = null,
    val lookup: SearchLookup? = null,
)

/**
 * The search only filters the Pokémon loaded so far (a few pages). When nothing matches, the
 * query is looked up in the whole Pokédex by exact name or number; [query] is the normalized
 * key that was sent to the API (see [PokemonListViewModel.lookupKey]).
 */
sealed interface SearchLookup {
    val query: String

    data class Loading(override val query: String) : SearchLookup
    data class Found(override val query: String, val pokemon: Pokemon) : SearchLookup

    /** The API knows no Pokémon by that exact name or number. */
    data class NotFound(override val query: String) : SearchLookup

    /** The lookup itself failed (no network): only the loaded Pokémon could be searched. */
    data class Failed(override val query: String, val message: String) : SearchLookup
}
