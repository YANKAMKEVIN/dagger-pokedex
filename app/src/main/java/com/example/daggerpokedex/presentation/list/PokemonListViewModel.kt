package com.example.daggerpokedex.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daggerpokedex.domain.model.Pokemon
import com.example.daggerpokedex.domain.usecase.GetPokemonDetailUseCase
import com.example.daggerpokedex.domain.usecase.GetPokemonListUseCase
import com.example.daggerpokedex.domain.util.Resource
import java.io.IOException
import kotlinx.coroutines.Job
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
    // Reused for the search: PokéAPI's detail endpoint accepts an exact name or a number.
    private val getPokemonDetail: GetPokemonDetailUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PokemonListState())
    val state: StateFlow<PokemonListState> = _state.asStateFlow()

    /** Index of the most recently loaded page (0-based). */
    private var currentPage = 0

    private var lookupJob: Job? = null

    init {
        loadFirstPage()
    }

    fun loadFirstPage() {
        currentPage = 0
        _state.update {
            it.copy(isLoading = true, errorMessage = null, loadMoreError = null, endReached = false)
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
        _state.update { it.copy(isLoadingMore = true, loadMoreError = null) }
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
                    // Keep what we already have; the footer offers to retry this same page
                    // (currentPage only moves forward on success).
                    it.copy(isLoadingMore = false, loadMoreError = result.message)
                }
            }
        }
    }

    fun retry() = loadFirstPage()

    /** Retry action of the footer shown when a further page failed. */
    fun retryNextPage() = loadNextPage()

    /**
     * Looks [query] up in the whole Pokédex, for when none of the loaded Pokémon match it.
     * A blank query clears the lookup; the same query is not fetched twice (unless it failed).
     */
    fun lookUp(query: String) {
        val key = lookupKey(query)
        val current = _state.value.lookup
        if (key == null) {
            lookupJob?.cancel()
            _state.update { it.copy(lookup = null) }
            return
        }
        if (current?.query == key && current !is SearchLookup.Failed) return

        lookupJob?.cancel()
        _state.update { it.copy(lookup = SearchLookup.Loading(key)) }
        lookupJob = viewModelScope.launch {
            val lookup = when (val result = getPokemonDetail(key)) {
                is Resource.Success -> SearchLookup.Found(
                    key,
                    Pokemon(id = result.data.id, name = result.data.name, imageUrl = result.data.imageUrl),
                )
                // Offline: we cannot tell whether the Pokémon exists.
                is Resource.Error ->
                    if (result.cause is IOException) SearchLookup.Failed(key, result.message)
                    else SearchLookup.NotFound(key)
            }
            _state.update { it.copy(lookup = lookup) }
        }
    }

    companion object {
        /**
         * Turns a search query into the key PokéAPI resolves: a number without its leading
         * zeros or "#" ("#025" -> "25"), or a lowercase name with hyphens ("Mr. Mime" ->
         * "mr-mime"). Null when there is nothing to look up.
         */
        fun lookupKey(query: String): String? {
            val q = query.trim().removePrefix("#").trim().lowercase()
            if (q.isEmpty()) return null
            if (q.all(Char::isDigit)) return q.trimStart('0').ifEmpty { null }
            return q.replace(".", "").replace("'", "").trim()
                .split(Regex("\\s+")).joinToString("-")
                .ifEmpty { null }
        }
    }
}
