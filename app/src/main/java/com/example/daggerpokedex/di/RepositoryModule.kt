package com.example.daggerpokedex.di

import com.example.daggerpokedex.data.repository.PokemonRepositoryImpl
import com.example.daggerpokedex.domain.repository.PokemonRepository
import com.example.daggerpokedex.di.scope.AppScope
import dagger.Binds
import dagger.Module

/**
 * `@Binds` vs `@Provides` — a question every Dagger learner hits.
 *
 * When you already have a concrete class that Dagger can build itself (here,
 * `PokemonRepositoryImpl` has an `@Inject` constructor) and you only need to tell
 * Dagger "use it wherever the interface is requested", use `@Binds`.
 *
 *   @Binds fun bindX(impl: Impl): Interface
 *
 * `@Binds` is:
 *   - more efficient: Dagger generates no extra factory, it just forwards the
 *     already-built implementation to the interface type;
 *   - abstract: the method has no body and lives in an `abstract class` (or
 *     interface) module, unlike `@Provides` which has real code in an `object`.
 *
 * Rule of thumb:
 *   - `@Binds`   -> "this implementation IS that interface" (one-liner).
 *   - `@Provides`-> "here is HOW to build this thing" (needs a body, e.g. Retrofit).
 *
 * The `@AppScope` here means a single repository instance is shared app-wide.
 */
@Module
abstract class RepositoryModule {

    @Binds
    @AppScope
    abstract fun bindPokemonRepository(impl: PokemonRepositoryImpl): PokemonRepository
}
