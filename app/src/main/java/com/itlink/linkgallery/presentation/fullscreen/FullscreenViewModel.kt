package com.itlink.linkgallery.presentation.fullscreen

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itlink.linkgallery.data.NetworkMonitor
import com.itlink.linkgallery.domain.model.ImageItem
import com.itlink.linkgallery.domain.usecase.GetImagesUseCase
import com.itlink.linkgallery.domain.usecase.RetryImageUseCase
import com.itlink.linkgallery.presentation.fullscreen.FullscreenReducer.idle
import com.itlink.linkgallery.presentation.fullscreen.FullscreenReducer.loading
import com.itlink.linkgallery.presentation.fullscreen.FullscreenReducer.online
import com.itlink.linkgallery.presentation.fullscreen.FullscreenReducer.showContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class FullscreenUiState(
    val items: List<ImageItem> = emptyList(),
    val isLoading: Boolean = false,
    val isOnline: Boolean = true
)

object FullscreenReducer {
    fun FullscreenUiState.loading() = copy(isLoading = true)
    fun FullscreenUiState.showContent() = copy(isLoading = false)
    fun FullscreenUiState.idle(items: List<ImageItem>) = copy(items = items, isLoading = false)
    fun FullscreenUiState.online(isOnline: Boolean) = copy(isOnline = isOnline)
}

@HiltViewModel
class FullscreenViewModel @Inject constructor(
    private val getImages: GetImagesUseCase,
    private val retry: RetryImageUseCase,
    private val monitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(FullscreenUiState(isLoading = true))
    val uiState: StateFlow<FullscreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getImages().collect { list ->
                _uiState.update { state -> state.idle(list.filter { it.isImage }) }
            }
        }
        viewModelScope.launch {
            monitor.isAvailable.collect { available ->
                _uiState.update { it.online(available) }
            }
        }
    }

    fun share(context: Context, item: ImageItem? = null) {
        val current = item ?: _uiState.value.items.firstOrNull() ?: return
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, current.url)
        }
        val chooser = Intent.createChooser(sendIntent, null)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun openInBrowser(context: Context, item: ImageItem? = null) {
        val current = item ?: _uiState.value.items.firstOrNull() ?: return
        val intent = Intent(Intent.ACTION_VIEW, current.url.toUri())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun retryCurrent(item: ImageItem? = null) {
        val current = item ?: _uiState.value.items.firstOrNull() ?: return
        viewModelScope.launch {
            _uiState.update { state -> state.loading() }
            retry(current.id)
            _uiState.update { state -> state.showContent() }
        }
    }
}
