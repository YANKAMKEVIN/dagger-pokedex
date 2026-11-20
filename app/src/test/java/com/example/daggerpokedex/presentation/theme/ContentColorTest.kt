package com.example.daggerpokedex.presentation.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentColorTest {

    @Test
    fun `light type colors get dark text`() {
        listOf("electric", "ice", "ground").forEach { type ->
            assertEquals(type, DarkInk, contentColorOn(colorForType(type)))
        }
    }

    @Test
    fun `saturated or dark type colors keep white text`() {
        listOf("fire", "water", "grass", "poison", "psychic", "dragon", "dark", "fighting").forEach { type ->
            assertEquals(type, Color.White, contentColorOn(colorForType(type)))
        }
    }
}
