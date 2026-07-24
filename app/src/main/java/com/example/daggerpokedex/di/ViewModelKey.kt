package com.example.daggerpokedex.di

import androidx.lifecycle.ViewModel
import dagger.MapKey
import kotlin.reflect.KClass

/**
 * A custom `@MapKey` used for Dagger MULTIBINDING.
 *
 * Multibinding lets many modules contribute entries into a single collection.
 * For ViewModels we build a `Map<Class<out ViewModel>, Provider<ViewModel>>`:
 * each ViewModel is one entry whose KEY is its class. This annotation is how we
 * declare that key when contributing an entry with `@IntoMap`:
 *
 *   @Binds @IntoMap @ViewModelKey(PokemonListViewModel::class)
 *   abstract fun bind(vm: PokemonListViewModel): ViewModel
 *
 * Dagger reads the `@ViewModelKey(...)` value and uses it as the map key.
 */
@MapKey
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewModelKey(val value: KClass<out ViewModel>)
