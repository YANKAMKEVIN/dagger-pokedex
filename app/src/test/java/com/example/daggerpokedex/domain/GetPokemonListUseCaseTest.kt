package com.example.daggerpokedex.domain

import com.example.daggerpokedex.domain.model.Pokemon
import com.example.daggerpokedex.domain.repository.PokemonRepository
import com.example.daggerpokedex.domain.usecase.GetPokemonListUseCase
import com.example.daggerpokedex.domain.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Because the use case depends only on the [PokemonRepository] abstraction, we
 * can hand it a mock — this is the payoff of dependency injection: no Retrofit,
 * no Android, just business logic under test.
 */
class GetPokemonListUseCaseTest {

    private val repository: PokemonRepository = mockk()
    private val useCase = GetPokemonListUseCase(repository)

    @Test
    fun `computes offset from page and forwards result`() = runTest {
        val expected = listOf(Pokemon(1, "Bulbasaur", "url"))
        coEvery { repository.getPokemonList(any(), any()) } returns Resource.Success(expected)

        val result = useCase(page = 2)

        assertEquals(Resource.Success(expected), result)
        // page 2 * PAGE_SIZE (40) => offset 80
        coVerify { repository.getPokemonList(limit = 40, offset = 80) }
    }
}
