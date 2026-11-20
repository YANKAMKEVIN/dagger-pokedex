package com.example.daggerpokedex.presentation.detail

import com.example.daggerpokedex.domain.model.PokemonDetail
import org.junit.Assert.assertEquals
import org.junit.Test

class StatFormattingTest {

    @Test
    fun `stat names from the mapper get Pokedex abbreviations`() {
        val names = listOf("Hp", "Attack", "Defense", "Special attack", "Special defense", "Speed")
        assertEquals(listOf("HP", "ATK", "DEF", "SP. ATK", "SP. DEF", "SPD"), names.map(::statLabel))
        assertEquals("SP. ATK", statLabel("special-attack"))
        assertEquals("ACCURACY", statLabel("Accuracy"))
    }

    @Test
    fun `total is the sum of the base stats`() {
        // Pikachu: 35 / 55 / 40 / 50 / 50 / 90.
        val stats = listOf(35, 55, 40, 50, 50, 90).mapIndexed { i, v -> PokemonDetail.Stat("s$i", v) }
        assertEquals(320, stats.total())
        assertEquals(0, emptyList<PokemonDetail.Stat>().total())
    }
}
