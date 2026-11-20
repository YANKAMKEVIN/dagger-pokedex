package com.example.daggerpokedex.presentation.detail

import com.example.daggerpokedex.domain.model.Pokemon

/** The Pokémon before and after the one shown, among those loaded in the list. */
data class Neighbors(val previous: Pokemon?, val next: Pokemon?)

/**
 * Finds the neighbors of [name] in the loaded [pokemons]. A Pokémon that is not in the list
 * (e.g. found by the whole-Pokédex search) has none.
 */
fun neighborsOf(pokemons: List<Pokemon>, name: String): Neighbors {
    val index = pokemons.indexOfFirst { it.name.equals(name, ignoreCase = true) }
    if (index < 0) return Neighbors(previous = null, next = null)
    return Neighbors(previous = pokemons.getOrNull(index - 1), next = pokemons.getOrNull(index + 1))
}
