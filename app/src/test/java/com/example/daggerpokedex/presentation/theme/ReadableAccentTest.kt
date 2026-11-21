package com.example.daggerpokedex.presentation.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Type-colored text on the detail page stays readable (WCAG AA, 4.5:1). */
class ReadableAccentTest {

    private val lightPage = Color(0xFFFEF7FF) // Material 3 light background
    private val darkPage = Color(0xFF1B1B2F) // the app's dark background

    @Test
    fun `contrast ratio spans 1 to 21`() {
        assertEquals(21f, contrastRatio(Color.Black, Color.White), 0.01f)
        assertEquals(1f, contrastRatio(PokeRed, PokeRed), 0.001f)
    }

    @Test
    fun `light type colors are darkened until readable on the light page`() {
        listOf("electric", "ice", "ground", "steel", "bug", "grass").forEach { type ->
            val accent = colorForType(type)
            val text = readableAccentOn(accent, lightPage)
            assertTrue("$type: ${contrastRatio(text, lightPage)}", contrastRatio(text, lightPage) >= 4.5f)
            assertTrue("$type should be darker", text.red + text.green + text.blue < accent.red + accent.green + accent.blue)
        }
    }

    @Test
    fun `dark type colors are lightened until readable on the dark page`() {
        listOf("dark", "ghost", "dragon", "fighting").forEach { type ->
            val text = readableAccentOn(colorForType(type), darkPage)
            assertTrue("$type: ${contrastRatio(text, darkPage)}", contrastRatio(text, darkPage) >= 4.5f)
        }
    }

    @Test
    fun `colors that are already readable are kept as they are`() {
        val fighting = colorForType("fighting")
        assertTrue(contrastRatio(fighting, lightPage) >= 4.5f)
        assertEquals(fighting, readableAccentOn(fighting, lightPage))
        val ice = colorForType("ice")
        assertEquals(ice, readableAccentOn(ice, darkPage))
    }
}
