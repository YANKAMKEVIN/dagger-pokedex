package com.example.daggerpokedex.presentation

import com.example.daggerpokedex.MainDispatcherRule
import com.example.daggerpokedex.domain.model.Pokemon
import com.example.daggerpokedex.domain.usecase.GetPokemonListUseCase
import com.example.daggerpokedex.domain.util.Resource
import com.example.daggerpokedex.presentation.list.PokemonListViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class PokemonListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPokemonList: GetPokemonListUseCase = mockk()

    @Test
    fun `emits success state when use case returns data`() = runTest {
        val pokemons = listOf(Pokemon(25, "Pikachu", "url"))
        coEvery { getPokemonList(any()) } returns Resource.Success(pokemons)

        val viewModel = PokemonListViewModel(getPokemonList)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(pokemons, state.pokemons)
    }

    @Test
    fun `emits error state when use case fails`() = runTest {
        coEvery { getPokemonList(any()) } returns Resource.Error("boom")

        val viewModel = PokemonListViewModel(getPokemonList)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("boom", state.errorMessage)
    }
}
