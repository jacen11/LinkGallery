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
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            actions = {
                val description = stringResource(R.string.cd_toggle_theme)
                IconButton(
                    onClick = { viewModel.toggleTheme() },
                    modifier = Modifier.semantics { 
                        contentDescription = description 
                    }
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
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ImageGrid(
    items: List<ImageItem>,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onItemClick: (ImageItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        contentPadding = PaddingValues(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = items,
            key = { it.id },
            contentType = { it.isImage }
        ) { item ->
            ImageGridCell(
                item = item,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
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
    onClick: () -> Unit
) {
    val thumbnailFile = remember(item.thumbnailPath) {
        item.thumbnailPath?.let { java.io.File(it) }
    }

    Card(
        onClick = onClick,
        modifier = Modifier
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
