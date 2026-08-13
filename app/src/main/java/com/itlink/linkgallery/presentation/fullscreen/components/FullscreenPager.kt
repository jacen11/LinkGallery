package com.itlink.linkgallery.presentation.fullscreen.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itlink.linkgallery.domain.model.ImageItem
import com.itlink.linkgallery.presentation.fullscreen.ZoomableImage

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FullscreenPager(
    items: List<ImageItem>,
    pagerState: PagerState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onImageTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize()
    ) { page ->
        val item = items[page]
        ZoomableImage(
            url = item.originalPath ?: item.url,
            imageId = item.id,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            onTap = onImageTap
        )
    }
}
