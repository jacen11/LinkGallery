package com.itlink.linkgallery.presentation.fullscreen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.itlink.linkgallery.domain.model.ImageItem
import com.itlink.linkgallery.presentation.fullscreen.components.FullscreenPager
import com.itlink.linkgallery.presentation.fullscreen.components.FullscreenTopAppBar
import com.itlink.linkgallery.presentation.fullscreen.components.NoItemsPlaceholder

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FullscreenScreen(
    imageId: String,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: FullscreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val startIndex = remember(uiState.items, imageId) { 
        uiState.items.indexOfFirst { it.id == imageId }.coerceAtLeast(0) 
    }
    
    var topBarVisible by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val view = LocalView.current
    var currentItem by remember { mutableStateOf<ImageItem?>(null) }

    LaunchedEffect(topBarVisible) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        if (topBarVisible) {
            controller.show(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        } else {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.items.isNotEmpty()) {
            val pagerState = rememberPagerState(initialPage = startIndex) { uiState.items.size }

            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }.collect { page ->
                    currentItem = uiState.items.getOrNull(page)
                }
            }

            BackHandler(enabled = topBarVisible) {
                navController.popBackStack()
            }

            FullscreenPager(
                items = uiState.items,
                pagerState = pagerState,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onImageTap = { topBarVisible = !topBarVisible }
            )

            if (topBarVisible) {
                FullscreenTopAppBar(
                    currentItem = currentItem,
                    onBackClick = { navController.popBackStack() },
                    onShareClick = { viewModel.share(context, it) },
                    onBrowserClick = { viewModel.openInBrowser(context, it) },
                    onRetryClick = { viewModel.retryCurrent(it) }
                )
            }
        } else if (!uiState.isLoading) {
            NoItemsPlaceholder()
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
