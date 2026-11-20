package com.example.daggerpokedex.presentation.detail

import com.example.daggerpokedex.domain.model.Pokemon
import org.junit.Assert.assertEquals
import org.junit.Test

class NeighborsTest {

    private val loaded = listOf(Pokemon(24, "Arbok", "a"), Pokemon(25, "Pikachu", "p"), Pokemon(26, "Raichu", "r"))

    @Test
    fun `previous and next come from the loaded list`() {
        assertEquals(Neighbors(loaded[0], loaded[2]), neighborsOf(loaded, "Pikachu"))
        assertEquals(Neighbors(loaded[0], loaded[2]), neighborsOf(loaded, "pikachu"))
    }

    @Test
    fun `the ends of the list have a single neighbor`() {
        assertEquals(Neighbors(null, loaded[1]), neighborsOf(loaded, "Arbok"))
        assertEquals(Neighbors(loaded[1], null), neighborsOf(loaded, "Raichu"))
    }

    @Test
    fun `a Pokemon outside the loaded list has no neighbors`() {
        assertEquals(Neighbors(null, null), neighborsOf(loaded, "Mewtwo"))
    }
}
