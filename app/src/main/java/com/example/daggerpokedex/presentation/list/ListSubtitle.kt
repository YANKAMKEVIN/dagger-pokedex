package com.example.daggerpokedex.presentation.list

/**
 * Live subtitle under the "Pokédex" title: how many Pokémon are loaded, or how many match
 * the search. Before anything is loaded (first load, error) it invites to search.
 */
fun listSubtitle(loadedCount: Int, query: String, resultCount: Int): String = when {
    loadedCount == 0 -> "Search a Pokémon by name or number"
    query.isBlank() -> "$loadedCount Pokémon loaded"
    resultCount == 1 -> "1 result"
    else -> "$resultCount results"
}
