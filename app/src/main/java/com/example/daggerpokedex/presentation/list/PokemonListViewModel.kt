package com.example.daggerpokedex.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daggerpokedex.domain.usecase.GetPokemonListUseCase
import com.example.daggerpokedex.domain.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MVVM ViewModel for the list screen. It holds UI state in a [StateFlow] and
 * exposes intents (`loadFirstPage`, `loadNextPage`, `retry`) the UI can call.
 *
 * ## Dagger angle
 * The `@Inject constructor` receives the use case straight from the graph. Note
 * what is NOT here: no `Retrofit`, no repository construction, no service
 * locator. The ViewModel depends only on the one abstraction it needs, and
 * Dagger supplies a fully-built instance. That is dependency injection doing its
 * job — this class is trivial to unit test because you can pass a fake use case.
 *
 * ## Pagination
 * Keyset-free offset paging: each call asks the use case for the next page. When
 * the API returns fewer than [GetPokemonListUseCase.PAGE_SIZE] items we know we
 * have hit the end and stop requesting more.
 */
class PokemonListViewModel @Inject constructor(
    private val getPokemonList: GetPokemonListUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PokemonListState())
    val state: StateFlow<PokemonListState> = _state.asStateFlow()

    /** Index of the most recently loaded page (0-based). */
    private var currentPage = 0

    init {
        loadFirstPage()
    }

    fun loadFirstPage() {
        currentPage = 0
        _state.update {
            it.copy(isLoading = true, errorMessage = null, endReached = false)
        }
        viewModelScope.launch {
            when (val result = getPokemonList(page = 0)) {
                is Resource.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        pokemons = result.data,
                        endReached = result.data.size < GetPokemonListUseCase.PAGE_SIZE,
                    )
                }

                is Resource.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    /** Called by the UI when the user scrolls near the end of the grid. */
    fun loadNextPage() {
        val current = _state.value
        // Guard against overlapping loads and against paging past the end.
        if (current.isLoading || current.isLoadingMore || current.endReached) return

        val nextPage = currentPage + 1
        _state.update { it.copy(isLoadingMore = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = getPokemonList(page = nextPage)) {
                is Resource.Success -> {
                    currentPage = nextPage
                    _state.update {
                        it.copy(
                            isLoadingMore = false,
                            pokemons = it.pokemons + result.data,
                            endReached = result.data.size < GetPokemonListUseCase.PAGE_SIZE,
                        )
                    }
                }

                is Resource.Error -> _state.update {
                    // Keep what we already have; just surface the error.
                    it.copy(isLoadingMore = false, errorMessage = result.message)
                }
            }
        }
    }

    fun retry() = loadFirstPage()
}
