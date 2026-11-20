package com.example.daggerpokedex.presentation

import com.example.daggerpokedex.presentation.list.listSubtitle
import org.junit.Assert.assertEquals
import org.junit.Test

class ListSubtitleTest {

    @Test
    fun `counts the loaded Pokemon while browsing`() {
        assertEquals("120 Pokémon loaded", listSubtitle(loadedCount = 120, query = "", resultCount = 120))
        assertEquals("40 Pokémon loaded", listSubtitle(loadedCount = 40, query = "  ", resultCount = 40))
    }

    @Test
    fun `counts the results while searching`() {
        assertEquals("3 results", listSubtitle(loadedCount = 40, query = "char", resultCount = 3))
        assertEquals("1 result", listSubtitle(loadedCount = 40, query = "mewtwo", resultCount = 1))
        assertEquals("0 results", listSubtitle(loadedCount = 40, query = "zzz", resultCount = 0))
    }

    @Test
    fun `invites to search before anything is loaded`() {
        assertEquals("Search a Pokémon by name or number", listSubtitle(loadedCount = 0, query = "", resultCount = 0))
    }
}
