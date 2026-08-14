package com.itlink.linkgallery.presentation.fullscreen.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.itlink.linkgallery.R
import com.itlink.linkgallery.domain.model.ImageItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullscreenTopAppBar(
    currentItem: ImageItem?,
    onBackClick: () -> Unit,
    onShareClick: (ImageItem?) -> Unit,
    onBrowserClick: (ImageItem?) -> Unit,
    onRetryClick: (ImageItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(R.string.preview_title)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
        },
        actions = {
            IconButton(onClick = { onShareClick(currentItem) }) {
                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share))
            }
            IconButton(onClick = { onBrowserClick(currentItem) }) {
                Icon(Icons.Default.Email, contentDescription = stringResource(R.string.open_in_browser))
            }
            IconButton(onClick = { onRetryClick(currentItem) }) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.retry_loading))
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}
