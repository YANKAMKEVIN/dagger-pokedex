package com.example.daggerpokedex.presentation.navigation

/**
 * A minimal, dependency-free navigation model. Keeping navigation as plain state
 * avoids pulling in a navigation library and keeps the focus of this sample on
 * Dagger. A real app might use Navigation-Compose instead.
 */
sealed interface Screen {
    data object List : Screen
    data class Detail(val name: String) : Screen
}
