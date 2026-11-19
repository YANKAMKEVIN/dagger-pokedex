package com.example.daggerpokedex.presentation

import com.example.daggerpokedex.MainDispatcherRule
import com.example.daggerpokedex.domain.model.Pokemon
import com.example.daggerpokedex.domain.usecase.GetPokemonListUseCase
import com.example.daggerpokedex.domain.util.Resource
import com.example.daggerpokedex.presentation.list.PokemonListViewModel
import io.mockk.coEvery
import io.mockk.coVerify
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

    private fun page(page: Int) = (1..GetPokemonListUseCase.PAGE_SIZE).map {
        val id = page * GetPokemonListUseCase.PAGE_SIZE + it
        Pokemon(id, "Pokemon $id", "url")
    }

    @Test
    fun `a failed next page keeps the list and is retried from the same page`() = runTest {
        coEvery { getPokemonList(0) } returns Resource.Success(page(0))
        coEvery { getPokemonList(1) } returns Resource.Error("offline") andThen Resource.Success(page(1))
        val viewModel = PokemonListViewModel(getPokemonList)

        viewModel.loadNextPage()

        val failed = viewModel.state.value
        assertEquals("offline", failed.loadMoreError)
        assertNull("first-page error screen must not be used", failed.errorMessage)
        assertEquals(page(0), failed.pokemons)

        viewModel.retryNextPage()

        val retried = viewModel.state.value
        assertNull(retried.loadMoreError)
        assertEquals(page(0) + page(1), retried.pokemons)
        coVerify(exactly = 1) { getPokemonList(0) }
        coVerify(exactly = 2) { getPokemonList(1) }
    }
}
