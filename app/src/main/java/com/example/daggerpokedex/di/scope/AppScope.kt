package com.example.daggerpokedex.di.scope

import javax.inject.Scope

/**
 * A CUSTOM SCOPE annotation.
 *
 * A scope tells Dagger "keep a single instance of this type alive for as long as
 * the component that owns the scope lives." Our [com.example.daggerpokedex.di.AppComponent]
 * is annotated `@AppScope`, so anything provided as `@AppScope` becomes an
 * application-wide singleton (created once, reused everywhere).
 *
 * `@Singleton` (from javax.inject) does the exact same thing — it is simply the
 * scope Dagger ships with. Defining our own makes the intent explicit and shows
 * how custom scopes are declared. A type may only be scoped by the component
 * that carries the matching scope annotation.
 *
 * `@Scope`      -> marks this annotation as a Dagger scope.
 * `@Retention(RUNTIME)` -> required so the annotation survives to where Dagger reads it.
 */
@Scope
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class AppScope
