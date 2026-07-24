package com.example.daggerpokedex.di.qualifier

import javax.inject.Qualifier

/**
 * QUALIFIERS disambiguate between multiple providers of the SAME type.
 *
 * Both the API base URL and, say, an image base URL are `String`s. If two
 * `@Provides` methods both returned `String`, Dagger could not tell them apart.
 * A qualifier annotation acts like a "name tag" on the binding:
 *
 *   @Provides @BaseUrl fun provideBaseUrl(): String = "..."   // tagged binding
 *   class X @Inject constructor(@BaseUrl url: String)          // asks for that tag
 *
 * `@Qualifier` is the standard `javax.inject` way; a typed annotation like this
 * is preferred over the stringly-typed `@Named("baseUrl")` because typos become
 * compile errors instead of silent mismatches.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class BaseUrl

/** Marks the CoroutineDispatcher used for IO (network/disk) work. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher
