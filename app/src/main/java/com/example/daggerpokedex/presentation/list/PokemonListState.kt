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
 */
data class PokemonListState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val pokemons: List<Pokemon> = emptyList(),
    val errorMessage: String? = null,
    val endReached: Boolean = false,
)
