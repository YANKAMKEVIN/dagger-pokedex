package com.example.daggerpokedex.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daggerpokedex.domain.usecase.GetPokemonDetailUseCase
import com.example.daggerpokedex.domain.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the detail screen.
 *
 * ## Why a `load(name)` method instead of a constructor argument?
 * The Pokémon name is a RUNTIME value chosen when the user taps a row — Dagger
 * cannot know it at graph-construction time. Rather than reach for AssistedInject
 * (a more advanced Dagger feature), we keep the constructor purely
 * dependency-injected and pass the runtime argument through a `load(name)` call
 * triggered by the UI. This keeps the DI story simple and easy to follow.
 * (The README's "Going further" section notes how AssistedInject would formalise
 * passing runtime arguments through the graph.)
 */
class PokemonDetailViewModel @Inject constructor(
    private val getPokemonDetail: GetPokemonDetailUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PokemonDetailState())
    val state: StateFlow<PokemonDetailState> = _state.asStateFlow()

    private var loadedName: String? = null

    /** Idempotent: safe to call from a Composable side-effect on recomposition. */
    fun load(name: String) {
        if (loadedName == name && _state.value.detail != null) return
        loadedName = name
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = getPokemonDetail(name)) {
                is Resource.Success -> _state.update {
                    it.copy(isLoading = false, detail = result.data, errorMessage = null)
                }

                is Resource.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
