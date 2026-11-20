package com.example.daggerpokedex.presentation

import com.example.daggerpokedex.MainDispatcherRule
import com.example.daggerpokedex.domain.model.Pokemon
import com.example.daggerpokedex.domain.model.PokemonDetail
import com.example.daggerpokedex.domain.usecase.GetPokemonDetailUseCase
import com.example.daggerpokedex.domain.usecase.GetPokemonListUseCase
import com.example.daggerpokedex.domain.util.Resource
import com.example.daggerpokedex.presentation.list.PokemonListViewModel
import com.example.daggerpokedex.presentation.list.SearchLookup
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** The search falls back to PokéAPI when none of the loaded Pokémon match. */
class SearchLookupTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPokemonList: GetPokemonListUseCase = mockk {
        coEvery { this@mockk(any()) } returns Resource.Success(listOf(Pokemon(1, "Bulbasaur", "url")))
    }
    private val getPokemonDetail: GetPokemonDetailUseCase = mockk()

    private val mewtwo = PokemonDetail(
        id = 150, name = "Mewtwo", imageUrl = "art/150", heightMeters = 2.0, weightKilograms = 122.0,
        types = listOf("Psychic"), stats = emptyList(), cryUrl = null,
    )

    @Test
    fun `lookup keys are what PokeAPI resolves`() {
        assertEquals("mewtwo", PokemonListViewModel.lookupKey("  Mewtwo "))
        assertEquals("25", PokemonListViewModel.lookupKey("025"))
        assertEquals("25", PokemonListViewModel.lookupKey("#25"))
        assertEquals("mr-mime", PokemonListViewModel.lookupKey("Mr. Mime"))
        assertEquals("farfetchd", PokemonListViewModel.lookupKey("Farfetch'd"))
        assertNull(PokemonListViewModel.lookupKey("   "))
        assertNull(PokemonListViewModel.lookupKey("#"))
        assertNull(PokemonListViewModel.lookupKey("000"))
    }

    @Test
    fun `a Pokemon that is not loaded yet is found in the whole Pokedex`() = runTest {
        coEvery { getPokemonDetail("mewtwo") } returns Resource.Success(mewtwo)
        val viewModel = PokemonListViewModel(getPokemonList, getPokemonDetail)

        viewModel.lookUp("Mewtwo")

        assertEquals(
            SearchLookup.Found("mewtwo", Pokemon(150, "Mewtwo", "art/150")),
            viewModel.state.value.lookup,
        )
        // Same query again (recomposition, typing pause): no second request.
        viewModel.lookUp("mewtwo ")
        coVerify(exactly = 1) { getPokemonDetail("mewtwo") }
    }

    @Test
    fun `an unknown name is reported as not found`() = runTest {
        coEvery { getPokemonDetail("pikachoo") } returns Resource.Error("Server error (404). Please try again.")
        val viewModel = PokemonListViewModel(getPokemonList, getPokemonDetail)

        viewModel.lookUp("pikachoo")

        assertEquals(SearchLookup.NotFound("pikachoo"), viewModel.state.value.lookup)
    }

    @Test
    fun `an offline lookup fails, can be retried, and a blank query clears it`() = runTest {
        coEvery { getPokemonDetail("150") } returns
            Resource.Error("No internet connection.", IOException()) andThen Resource.Success(mewtwo)
        val viewModel = PokemonListViewModel(getPokemonList, getPokemonDetail)

        viewModel.lookUp("150")
        assertEquals(SearchLookup.Failed("150", "No internet connection."), viewModel.state.value.lookup)

        viewModel.lookUp("150")
        assertEquals(SearchLookup.Found("150", Pokemon(150, "Mewtwo", "art/150")), viewModel.state.value.lookup)

        viewModel.lookUp(" ")
        assertNull(viewModel.state.value.lookup)
    }
}
