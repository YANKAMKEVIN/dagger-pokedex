package com.example.daggerpokedex.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

/**
 * The bridge between Dagger and the Android ViewModel system.
 *
 * Android insists on creating ViewModels through a `ViewModelProvider.Factory`
 * (so it can survive configuration changes). Dagger, on the other hand, wants to
 * build objects itself. This class reconciles the two:
 *
 *  1. Dagger injects a `Map<Class<out ViewModel>, Provider<ViewModel>>` — every
 *     ViewModel contributed with `@IntoMap` + `@ViewModelKey` (see ViewModelModule).
 *     A `Provider<T>` is Dagger's lazy factory for `T`: calling `.get()` builds a
 *     fresh, fully-injected instance on demand.
 *  2. When Android asks this factory for `SomeViewModel::class`, we look that
 *     class up in the map and call its provider — returning a ViewModel with all
 *     its use cases already injected.
 *
 * Result: ViewModels get full constructor injection, yet Android still owns their
 * lifecycle. This is THE canonical "how do I inject a ViewModel with pure Dagger"
 * pattern.
 */
class DaggerViewModelFactory @Inject constructor(
    private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val provider = creators[modelClass]
            ?: creators.entries.firstOrNull { modelClass.isAssignableFrom(it.key) }?.value
            ?: throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")

        @Suppress("UNCHECKED_CAST")
        return provider.get() as T
    }
}
