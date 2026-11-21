package com.example.daggerpokedex.presentation

import com.example.daggerpokedex.domain.model.Pokemon
import com.example.daggerpokedex.presentation.list.matchesQuery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The client-side filter of the loaded Pokémon. */
class ListSearchTest {

    private val pikachu = Pokemon(25, "Pikachu", "art/25")
    private val mrMime = Pokemon(122, "Mr-mime", "art/122")
    private val loaded = listOf(Pokemon(1, "Bulbasaur", "art/1"), pikachu, mrMime, Pokemon(250, "Ho-oh", "art/250"))

    @Test
    fun `plain name and number queries still match as before`() {
        assertTrue(pikachu.matchesQuery("pika"))
        assertTrue(pikachu.matchesQuery("  PIKA "))
        assertTrue(pikachu.matchesQuery("25"))
        assertTrue(pikachu.matchesQuery(""))
        assertFalse(pikachu.matchesQuery("bulba"))
    }

    @Test
    fun `numbers written with a hash or leading zeros match the loaded Pokemon`() {
        assertTrue(pikachu.matchesQuery("#25"))
        assertTrue(pikachu.matchesQuery("025"))
        assertTrue(pikachu.matchesQuery("# 025"))
        assertEquals(listOf(250), loaded.filter { it.matchesQuery("#0250") }.map { it.id })
    }

    @Test
    fun `names written with dots or spaces match the hyphenated entry`() {
        assertTrue(mrMime.matchesQuery("Mr. Mime"))
        assertTrue(mrMime.matchesQuery("mr mime"))
        assertTrue(loaded.single { it.matchesQuery("ho oh") }.name == "Ho-oh")
        assertFalse(pikachu.matchesQuery("Mr. Mime"))
    }
}
