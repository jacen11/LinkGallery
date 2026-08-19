package com.itlink.linkgallery.presentation.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Grid : Screen("grid")
    data class Fullscreen(val id: String) : Screen("fullscreen/{id}") {
        companion object {
            const val route = "fullscreen/{id}"
            const val arg = "id"
            fun createRoute(id: String) = "fullscreen/$id"
        }
    }
}
