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
        title = { Text("Preview") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = { onShareClick(currentItem) }) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
            IconButton(onClick = { onBrowserClick(currentItem) }) {
                Icon(Icons.Default.Email, contentDescription = "Open")
            }
            IconButton(onClick = { onRetryClick(currentItem) }) {
                Icon(Icons.Default.Refresh, contentDescription = "Retry")
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}
