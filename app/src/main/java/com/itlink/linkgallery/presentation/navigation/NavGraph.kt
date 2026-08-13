package com.itlink.linkgallery.presentation.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.itlink.linkgallery.presentation.fullscreen.FullscreenScreen
import com.itlink.linkgallery.presentation.grid.GridScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = Screen.Grid.route,
            modifier = modifier
        ) {
            composable(Screen.Grid.route) {
                GridScreen(
                    navController = navController,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable
                )
            }
            composable(
                route = Screen.Fullscreen.route,
                arguments = listOf(navArgument(Screen.Fullscreen.arg) {
                    type = NavType.StringType
                })
            ) {
                FullscreenScreen(
                    imageId = it.arguments?.getString(Screen.Fullscreen.arg) ?: "",
                    navController = navController,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable
                )
            }
        }
    }
}
