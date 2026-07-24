package com.example.daggerpokedex.presentation.detail

import com.example.daggerpokedex.domain.model.PokemonDetail

data class PokemonDetailState(
    val isLoading: Boolean = false,
    val detail: PokemonDetail? = null,
    val errorMessage: String? = null,
)
