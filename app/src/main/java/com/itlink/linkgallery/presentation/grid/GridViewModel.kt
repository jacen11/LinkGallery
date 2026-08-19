package com.itlink.linkgallery.presentation.grid

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itlink.linkgallery.data.NetworkMonitor
import com.itlink.linkgallery.data.SettingsDataStore
import com.itlink.linkgallery.domain.model.ImageItem
import com.itlink.linkgallery.domain.usecase.GetImagesUseCase
import com.itlink.linkgallery.domain.usecase.ProcessPendingUseCase
import com.itlink.linkgallery.domain.usecase.RefreshImagesUseCase
import com.itlink.linkgallery.domain.usecase.RetryImageUseCase
import com.itlink.linkgallery.presentation.grid.GridReducer.darkMode
import com.itlink.linkgallery.presentation.grid.GridReducer.idle
import com.itlink.linkgallery.presentation.grid.GridReducer.loading
import com.itlink.linkgallery.presentation.grid.GridReducer.onboarding
import com.itlink.linkgallery.presentation.grid.GridReducer.online
import com.itlink.linkgallery.presentation.grid.GridReducer.refreshing
import com.itlink.linkgallery.presentation.grid.GridReducer.showContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class GridUiState(
    val items: List<ImageItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isDarkMode: Boolean = false,
    val isOnline: Boolean = true,
    val onboardingStep: OnboardingStep? = null
)

sealed class OnboardingStep {
    data object ThemeToggle : OnboardingStep()
    data object GridItem : OnboardingStep()
}

object GridReducer {
    fun GridUiState.loading() = copy(isLoading = true)
    fun GridUiState.refreshing() = copy(isRefreshing = true)
    fun GridUiState.showContent() = copy(isLoading = false, isRefreshing = false)
    fun GridUiState.idle(items: List<ImageItem>) = copy(items = items, isLoading = false, isRefreshing = false)
    fun GridUiState.darkMode(enabled: Boolean) = copy(isDarkMode = enabled)
    fun GridUiState.online(isOnline: Boolean) = copy(isOnline = isOnline)
    fun GridUiState.onboarding(step: OnboardingStep?) = copy(onboardingStep = step)
}

@HiltViewModel
class GridViewModel @Inject constructor(
    private val getImages: GetImagesUseCase,
    private val refresh: RefreshImagesUseCase,
    private val processPending: ProcessPendingUseCase,
    private val retry: RetryImageUseCase,
    private val monitor: NetworkMonitor,
    private val dataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(GridUiState(isLoading = true))
    val uiState: StateFlow<GridUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getImages()
                .conflate()
                .collect { list ->
                    _uiState.update { it.idle(list) }
                }
        }
        viewModelScope.launch {
            monitor.isAvailable.collect { available ->
                _uiState.update { it.online(available) }
                if (available) {
                    refresh()
                    processPending()
                    _uiState.value.items.filter { it.status == ImageItem.Status.Error }
                        .forEach { retry(it.id) }
                }
            }
        }
        viewModelScope.launch {
            dataStore.isDarkMode.collect { enabled ->
                _uiState.update { it.darkMode(enabled) }
            }
        }
        viewModelScope.launch {
            val completed = dataStore.isOnboardingCompleted.first()
            if (!completed) {
                _uiState.update { it.onboarding(OnboardingStep.ThemeToggle) }
            }
        }
    }

    fun nextOnboardingStep() {
        val next = when (_uiState.value.onboardingStep) {
            OnboardingStep.ThemeToggle -> OnboardingStep.GridItem
            OnboardingStep.GridItem -> {
                viewModelScope.launch { dataStore.setOnboardingCompleted(true) }
                null
            }
            null -> null
        }
        _uiState.update { it.onboarding(next) }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val newMode = !_uiState.value.isDarkMode
            dataStore.setDarkMode(newMode)
        }
    }

    fun retryItem(id: String) {
        viewModelScope.launch {
            _uiState.update { it.loading() }
            retry(id)
            _uiState.update { it.showContent() }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.refreshing() }
            refresh()
            processPending()
            _uiState.update { it.showContent() }
        }
    }
}
