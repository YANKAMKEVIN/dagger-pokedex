@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.example.daggerpokedex.presentation.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Shared-element transition plumbing.
 *
 * `SharedTransitionLayout` exposes a [SharedTransitionScope], and the
 * `AnimatedContent` that swaps list ⇆ detail exposes an [AnimatedVisibilityScope].
 * The shared modifiers need both. Rather than thread these two scopes through
 * every composable signature, we publish them via CompositionLocals and read them
 * where needed. When they are absent (previews, tests), the helpers are no-ops.
 */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/** Duration of the card ⇄ header morph. */
private const val TRANSITION_MS = 420
private val pokemonBounds = BoundsTransform { _, _ -> tween(TRANSITION_MS) }

/**
 * Tags a composable as a shared ELEMENT (e.g. the artwork): the same content that
 * exists in both screens and should fly/scale between them. Matched by [key].
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.pokemonSharedElement(key: Any): Modifier {
    val shared = LocalSharedTransitionScope.current ?: return this
    val animated = LocalNavAnimatedScope.current ?: return this
    return with(shared) {
        this@pokemonSharedElement.sharedElement(
            rememberSharedContentState(key = key),
            animatedVisibilityScope = animated,
            boundsTransform = pokemonBounds,
        )
    }
}

/**
 * Tags a composable as shared BOUNDS (e.g. the card background → detail header):
 * the container morphs its size/position and cross-fades its differing content.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.pokemonSharedBounds(key: Any): Modifier {
    val shared = LocalSharedTransitionScope.current ?: return this
    val animated = LocalNavAnimatedScope.current ?: return this
    return with(shared) {
        this@pokemonSharedBounds.sharedBounds(
            rememberSharedContentState(key = key),
            animatedVisibilityScope = animated,
            boundsTransform = pokemonBounds,
        )
    }
}
