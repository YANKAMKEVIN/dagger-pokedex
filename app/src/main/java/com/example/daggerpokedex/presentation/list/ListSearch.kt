package com.example.daggerpokedex.presentation.list

import com.example.daggerpokedex.domain.model.Pokemon

/**
 * Whether [this] loaded Pokémon matches the search [query], by name or Pokédex number.
 *
 * Besides the raw text, the query is compared in the same normalized form as the whole-Pokédex
 * lookup ([PokemonListViewModel.lookupKey]): "#025" or "025" find #25, "Mr. Mime" finds the
 * "Mr-mime" entry. Without it, those queries found nothing among the loaded Pokémon and went to
 * the network (and, offline, reported the Pokémon as not loaded although it was on screen).
 */
fun Pokemon.matchesQuery(query: String): Boolean {
    val q = query.trim()
    if (q.isEmpty()) return true
    if (name.contains(q, ignoreCase = true) || id.toString().contains(q)) return true
    val key = PokemonListViewModel.lookupKey(q) ?: return false
    return if (key.all(Char::isDigit)) {
        id.toString().contains(key)
    } else {
        PokemonListViewModel.lookupKey(name)?.contains(key) == true
    }
}
