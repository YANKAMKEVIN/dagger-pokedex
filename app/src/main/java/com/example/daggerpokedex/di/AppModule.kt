package com.example.daggerpokedex.di

import com.example.daggerpokedex.di.qualifier.IoDispatcher
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * A MODULE is a recipe book: a class of `@Provides` methods that tell Dagger how
 * to build types it cannot construct on its own (interfaces, third-party
 * classes, or values that need configuration).
 *
 * This module provides the IO dispatcher. `Dispatchers.IO` is a value we cannot
 * annotate with `@Inject`, so we wrap it in a `@Provides` method and tag it with
 * the `@IoDispatcher` qualifier so it can be told apart from any other
 * `CoroutineDispatcher` the graph might hold.
 *
 * Because the method is unscoped, Dagger calls it every time — which is fine, a
 * dispatcher is cheap and stateless.
 */
@Module
object AppModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
