package com.example.daggerpokedex

import android.app.Application
import com.example.daggerpokedex.di.AppComponent
import com.example.daggerpokedex.di.DaggerAppComponent

/**
 * The custom [Application] owns the root Dagger graph.
 *
 * The application object lives for the entire process, which makes it the
 * natural home for the app-scoped [AppComponent]. Everything scoped `@AppScope`
 * shares the component stored here, so there is exactly one OkHttpClient, one
 * Retrofit, one repository, etc. for the whole app.
 *
 * `DaggerAppComponent` does not exist until you build the project — it is the
 * class Dagger's annotation processor GENERATES from our `@Component` interface.
 * If your IDE flags it red before the first build, that is expected.
 */
class DaggerPokedexApp : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        // Build the graph exactly once, when the process starts.
        appComponent = DaggerAppComponent.factory().create()
    }
}
