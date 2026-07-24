package com.example.daggerpokedex.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.daggerpokedex.presentation.detail.PokemonDetailViewModel
import com.example.daggerpokedex.presentation.list.PokemonListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Registers every ViewModel into the multibound map consumed by
 * [DaggerViewModelFactory], and binds the factory itself to the
 * `ViewModelProvider.Factory` type Android expects.
 *
 * How one entry works:
 *
 *   @Binds                                   // "this Impl IS a ViewModel"
 *   @IntoMap                                 // "add it to the ViewModel map"
 *   @ViewModelKey(PokemonListViewModel::class) // "...under this key"
 *   abstract fun bind(vm: PokemonListViewModel): ViewModel
 *
 * Adding a new screen is now a one-line change: give its ViewModel an `@Inject`
 * constructor and add one `@Binds @IntoMap @ViewModelKey(...)` entry here.
 */
@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: DaggerViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(PokemonListViewModel::class)
    abstract fun bindPokemonListViewModel(vm: PokemonListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PokemonDetailViewModel::class)
    abstract fun bindPokemonDetailViewModel(vm: PokemonDetailViewModel): ViewModel
}
