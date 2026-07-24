package com.example.daggerpokedex.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.daggerpokedex.DaggerPokedexApp
import com.example.daggerpokedex.domain.model.Pokemon
import com.example.daggerpokedex.presentation.components.LocalNavAnimatedScope
import com.example.daggerpokedex.presentation.components.LocalSharedTransitionScope
import com.example.daggerpokedex.presentation.components.rememberMusicController
import com.example.daggerpokedex.presentation.detail.PokemonDetailScreen
import com.example.daggerpokedex.presentation.detail.PokemonDetailViewModel
import com.example.daggerpokedex.presentation.list.PokemonListScreen
import com.example.daggerpokedex.presentation.list.PokemonListViewModel
import com.example.daggerpokedex.presentation.splash.SplashScreen
import com.example.daggerpokedex.presentation.theme.DaggerPokedexTheme
import javax.inject.Inject

/**
 * The single Activity, hosting all Compose UI.
 *
 * ## Dagger angle: FIELD injection
 * Android constructs the Activity for us, so we cannot use constructor injection
 * here. Instead we:
 *   1. declare the dependency as an `@Inject lateinit var` field, then
 *   2. ask the AppComponent to fill it: `appComponent.inject(this)`.
 * This is "members injection" — the second style of pulling things out of a
 * component (the first being provision methods like `viewModelFactory()`).
 *
 * The injected [ViewModelProvider.Factory] is our Dagger-built
 * `DaggerViewModelFactory`, which we then hand to Compose's `viewModel(...)` so
 * every screen's ViewModel is created by Dagger with its dependencies wired in.
 */
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        // Inject fields BEFORE using them. Grab the app-wide component from the
        // Application and let Dagger populate `viewModelFactory`.
        (application as DaggerPokedexApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)

        setContent {
            DaggerPokedexTheme {
                AppRoot(viewModelFactory = viewModelFactory)
            }
        }
    }
}

/**
 * Tiny navigation host: `selectedName == null` shows the list, otherwise the
 * detail screen for that Pokémon. Both ViewModels are obtained through the
 * Dagger factory, so they arrive fully injected.
 *
 * ## Shared-element transition
 * The list ⇆ detail swap runs inside a [SharedTransitionLayout] + `AnimatedContent`
 * so the tapped card's artwork and colored container morph into the detail header.
 * The scopes needed by the shared modifiers are published via CompositionLocals
 * (see `components/SharedTransition.kt`).
 *
 * We also remember the tapped [Pokemon] as a `seed`. The detail data loads
 * asynchronously, so without a seed the header (the transition's destination)
 * would not exist during the animation. The seed lets the header render its
 * artwork/name immediately, and its card color is cross-faded to the type color
 * once the detail arrives.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun AppRoot(viewModelFactory: ViewModelProvider.Factory) {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var selectedName by rememberSaveable { mutableStateOf<String?>(null) }
    var seed by remember { mutableStateOf<Pokemon?>(null) }

    val musicController = rememberMusicController()
    var isMusicPlaying by rememberSaveable { mutableStateOf(false) }

    // Sync the controller with the saved state on first composition or state change
    LaunchedEffect(isMusicPlaying) {
        musicController.setPlaying(isMusicPlaying)
    }

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
        return
    }

    SharedTransitionLayout {
        val sharedScope = this
        AnimatedContent(
            targetState = selectedName,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "list-detail",
        ) { name ->
            CompositionLocalProvider(
                LocalSharedTransitionScope provides sharedScope,
                LocalNavAnimatedScope provides this@AnimatedContent,
            ) {
                if (name == null) {
                    val listViewModel: PokemonListViewModel = viewModel(factory = viewModelFactory)
                    PokemonListScreen(
                        viewModel = listViewModel,
                        isMusicPlaying = isMusicPlaying,
                        showMusicToggle = musicController.hasTrack,
                        onToggleMusic = {
                            val newState = !isMusicPlaying
                            isMusicPlaying = newState
                            musicController.setPlaying(newState)
                        },
                        onPokemonClick = { pokemon ->
                            seed = pokemon
                            selectedName = pokemon.name
                        },
                    )
                } else {
                    // Intercept system Back so it returns to the list instead of
                    // finishing the Activity (which would close the app).
                    BackHandler { selectedName = null }

                    // Key the ViewModel by name so each detail gets its OWN fresh
                    // Dagger-built instance (no stale previous-Pokémon state).
                    val detailViewModel: PokemonDetailViewModel =
                        viewModel(key = name, factory = viewModelFactory)
                    PokemonDetailScreen(
                        name = name,
                        seed = seed?.takeIf { it.name == name },
                        viewModel = detailViewModel,
                        onBack = { selectedName = null },
                    )
                }
            }
        }
    }
}
