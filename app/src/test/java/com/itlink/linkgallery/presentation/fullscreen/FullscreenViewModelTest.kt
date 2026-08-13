package com.itlink.linkgallery.presentation.fullscreen

import com.itlink.linkgallery.data.NetworkMonitor
import com.itlink.linkgallery.domain.model.ImageItem
import com.itlink.linkgallery.domain.usecase.GetImagesUseCase
import com.itlink.linkgallery.domain.usecase.RetryImageUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class FullscreenViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getImagesUseCase = mockk<GetImagesUseCase>()
    private val retryUseCase = mockk<RetryImageUseCase>()
    private val monitor = mockk<NetworkMonitor>(relaxed = true)
    
    private lateinit var viewModel: FullscreenViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { monitor.isAvailable } returns kotlinx.coroutines.flow.MutableStateFlow(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has loading true then false after collection`() = runTest {
        every { getImagesUseCase() } returns flowOf(emptyList())
        
        viewModel = FullscreenViewModel(
            getImages = getImagesUseCase,
            retry = retryUseCase,
            monitor = monitor
        )

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.items.isEmpty())
    }

    @Test
    fun `when getImages emits items, isLoading becomes false and items are updated`() = runTest {
        val testItems = listOf(
            ImageItem("1", "url1", true, "thumb1", "orig1", ImageItem.Status.Ready)
        )
        every { getImagesUseCase() } returns flowOf(testItems)

        viewModel = FullscreenViewModel(
            getImages = getImagesUseCase,
            retry = retryUseCase,
            monitor = monitor
        )

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(testItems, viewModel.uiState.value.items)
    }

    @Test
    fun `retryCurrent calls retry use case`() = runTest {
        val testItems = listOf(
            ImageItem("1", "url1", true, "thumb1", "orig1", ImageItem.Status.Error)
        )
        every { getImagesUseCase() } returns flowOf(testItems)
        coEvery { retryUseCase(any()) } returns Result.success(Unit)

        viewModel = FullscreenViewModel(
            getImages = getImagesUseCase,
            retry = retryUseCase,
            monitor = monitor
        )

        viewModel.retryCurrent()

        coVerify { retryUseCase("1") }
    }
}
