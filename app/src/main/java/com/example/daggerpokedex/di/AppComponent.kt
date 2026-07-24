package com.example.daggerpokedex.di

import androidx.lifecycle.ViewModelProvider
import com.example.daggerpokedex.presentation.MainActivity
import com.example.daggerpokedex.di.scope.AppScope
import dagger.Component

/**
 * The COMPONENT is the heart of the graph — the piece that ties every module
 * together and hands out fully-built objects.
 *
 * At build time Dagger generates an implementation called `DaggerAppComponent`
 * (prefix "Dagger" + component name). That generated class contains all the
 * wiring: it knows how to build the OkHttpClient, the Retrofit service, the
 * repository, every use case and every ViewModel, in the right order.
 *
 * ## What the annotations mean here
 * - `@AppScope`  -> this component's lifetime is the whole application, so any
 *   `@AppScope` provider becomes an app-wide singleton within it.
 * - `modules = [...]` -> the recipe books Dagger may use to satisfy requests.
 *
 * ## The two ways to GET things out of a component
 * 1. Provision method — `viewModelFactory()` below returns a type directly. We
 *    use it to pull the Dagger-built ViewModel factory into Compose.
 * 2. Members injection — `inject(activity)` fills in the `@Inject` fields of an
 *    object Android created for us (the Activity). Android instantiates the
 *    Activity, so we cannot use constructor injection there; instead we ask the
 *    component to inject its fields.
 *
 * ## Component factory
 * `@Component.Factory` lets us pass runtime values into the graph when we build
 * it. Here there is nothing external to pass, so it is parameterless; a common
 * real-world use is `create(@BindsInstance context: Context)`.
 */
@AppScope
@Component(
    modules = [
        AppModule::class,
        NetworkModule::class,
        RepositoryModule::class,
        ViewModelModule::class,
    ],
)
interface AppComponent {

    /** Provision method: exposes the Dagger-built ViewModel factory to the UI. */
    fun viewModelFactory(): ViewModelProvider.Factory

    /** Members injection: populates the @Inject fields of [MainActivity]. */
    fun inject(activity: MainActivity)

    @Component.Factory
    interface Factory {
        fun create(): AppComponent
    }
}
