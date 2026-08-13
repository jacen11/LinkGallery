package com.itlink.linkgallery.presentation.grid

import com.itlink.linkgallery.data.NetworkMonitor
import com.itlink.linkgallery.data.SettingsDataStore
import com.itlink.linkgallery.domain.model.ImageItem
import com.itlink.linkgallery.domain.usecase.GetImagesUseCase
import com.itlink.linkgallery.domain.usecase.ProcessPendingUseCase
import com.itlink.linkgallery.domain.usecase.RefreshImagesUseCase
import com.itlink.linkgallery.domain.usecase.RetryImageUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GridViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getImagesUseCase = mockk<GetImagesUseCase>()
    private val refreshUseCase = mockk<RefreshImagesUseCase>(relaxed = true)
    private val processPendingUseCase = mockk<ProcessPendingUseCase>(relaxed = true)
    private val retryUseCase = mockk<RetryImageUseCase>(relaxed = true)
    private val monitor = mockk<NetworkMonitor>(relaxed = true)
    private val dataStore = mockk<SettingsDataStore>(relaxed = true)

    private lateinit var viewModel: GridViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { monitor.isAvailable } returns MutableStateFlow(true)
        every { dataStore.isDarkMode } returns MutableStateFlow(false)
        every { getImagesUseCase() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has items updated from use case`() = runTest {
        initViewModel()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.items.isEmpty())
    }

    @Test
    fun `when getImages emits items, uiState is updated`() = runTest {
        val testItems = listOf(
            ImageItem("1", "url1", true, "thumb1", "orig1", ImageItem.Status.Ready)
        )
        every { getImagesUseCase() } returns flowOf(testItems)

        initViewModel()

        assertEquals(testItems, viewModel.uiState.value.items)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `toggleTheme calls dataStore setDarkMode`() = runTest {
        val isDarkModeFlow = MutableStateFlow(false)
        every { dataStore.isDarkMode } returns isDarkModeFlow
        
        initViewModel()

        viewModel.toggleTheme()

        coVerify { dataStore.setDarkMode(true) }
    }

    @Test
    fun `retryItem calls retry use case`() = runTest {
        initViewModel()

        viewModel.retryItem("img_5")

        coVerify { retryUseCase("img_5") }
    }

    private fun initViewModel() {
        viewModel = GridViewModel(
            getImages = getImagesUseCase,
            refresh = refreshUseCase,
            processPending = processPendingUseCase,
            retry = retryUseCase,
            monitor = monitor,
            dataStore = dataStore
        )
    }
}
