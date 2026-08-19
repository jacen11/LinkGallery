package com.itlink.linkgallery.presentation.grid

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.itlink.linkgallery.R
import com.itlink.linkgallery.domain.model.ImageItem
import com.itlink.linkgallery.presentation.common.CoachMark
import com.itlink.linkgallery.presentation.common.NoInternetScreen
import com.itlink.linkgallery.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun GridScreen(
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: GridViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var themeToggleCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var firstItemCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    val description = stringResource(R.string.cd_toggle_theme)
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier
                            .onGloballyPositioned { themeToggleCoordinates = it }
                            .semantics { contentDescription = description }
                    ) {
                        Text(
                            text = if (uiState.isDarkMode) "☀️" else "🌙",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            )
            
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refreshData() },
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.items.isEmpty() && !uiState.isOnline) {
                    NoInternetScreen(onRetry = { viewModel.refreshData() })
                } else if (uiState.isLoading && uiState.items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    ImageGrid(
                        items = uiState.items,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onFirstItemPositioned = { firstItemCoordinates = it },
                        onItemClick = { item ->
                            if (item.status == ImageItem.Status.Error) {
                                viewModel.retryItem(item.id)
                            } else {
                                navController.navigate(Screen.Fullscreen.createRoute(item.id))
                            }
                        }
                    )
                }
            }
        }

        // Onboarding Overlay
        uiState.onboardingStep?.let { step ->
            val target = when (step) {
                OnboardingStep.ThemeToggle -> themeToggleCoordinates
                OnboardingStep.GridItem -> firstItemCoordinates
            }
            val text = when (step) {
                OnboardingStep.ThemeToggle -> "Здесь можно переключить тему оформления на темную или светлую."
                OnboardingStep.GridItem -> "Нажмите на любое изображение, чтобы открыть его в полноэкранном режиме."
            }

            CoachMark(
                targetCoordinates = target,
                text = text,
                onNext = { viewModel.nextOnboardingStep() }
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ImageGrid(
    items: List<ImageItem>,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onFirstItemPositioned: (LayoutCoordinates) -> Unit,
    onItemClick: (ImageItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        contentPadding = PaddingValues(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> item.id },
            contentType = { _, item -> item.isImage }
        ) { index, item ->
            ImageGridCell(
                item = item,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = if (index == 0) Modifier.onGloballyPositioned(onFirstItemPositioned) else Modifier,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ImageGridCell(
    item: ImageItem,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val thumbnailFile = remember(item.thumbnailPath) {
        item.thumbnailPath?.let { java.io.File(it) }
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(1f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                item.status == ImageItem.Status.Ready && thumbnailFile != null -> {
                    with(sharedTransitionScope) {
                        AsyncImage(
                            model = thumbnailFile,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .sharedElement(
                                    rememberSharedContentState(key = "image_${item.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope
                                ),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                item.status == ImageItem.Status.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("!", style = MaterialTheme.typography.headlineMedium)
                    }
                }
                item.isImage -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.fillMaxSize(0.3f))
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("?", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
    }
}
