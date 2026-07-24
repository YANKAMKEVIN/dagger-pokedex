package com.example.daggerpokedex.domain.usecase

import com.example.daggerpokedex.domain.model.PokemonDetail
import com.example.daggerpokedex.domain.repository.PokemonRepository
import com.example.daggerpokedex.domain.util.Resource
import javax.inject.Inject

/** Fetches the rich detail for a single Pokémon by name. */
class GetPokemonDetailUseCase @Inject constructor(
    private val repository: PokemonRepository,
) {
    suspend operator fun invoke(name: String): Resource<PokemonDetail> =
        repository.getPokemonDetail(name)
}
