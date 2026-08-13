package com.itlink.linkgallery.data.repository

import com.itlink.linkgallery.data.FileCache
import com.itlink.linkgallery.data.local.ImageDao
import com.itlink.linkgallery.data.local.ImageEntity
import com.itlink.linkgallery.data.remote.RemoteDataSource
import com.itlink.linkgallery.domain.model.ImageItem
import com.itlink.linkgallery.util.ImageDownloader
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

import io.mockk.mockkStatic
import io.mockk.unmockkStatic

@OptIn(ExperimentalCoroutinesApi::class)
class ImageRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private val remote = mockk<RemoteDataSource>()
    private val dao = mockk<ImageDao>(relaxed = true)
    private val cache = mockk<FileCache>(relaxed = true)
    private val downloader = mockk<ImageDownloader>()

    private lateinit var repository: ImageRepositoryImpl

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        repository = ImageRepositoryImpl(remote, dao, cache, downloader)
    }

    @After
    fun tearDown() {
        unmockkStatic(Dispatchers::class)
        Dispatchers.resetMain()
    }

    @Test
    fun `getImages returns mapped items from dao`() = runTest {
        val entities = listOf(
            ImageEntity("1", "url1", true, "thumb1", "orig1", "Ready")
        )
        every { dao.getAll() } returns flowOf(entities)

        val result = repository.getImages().first()

        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
        assertEquals(ImageItem.Status.Ready, result[0].status)
    }

    @Test
    fun `refresh fetches remote and inserts into dao`() = runTest {
        val remoteLines = listOf("https://test.com/img.jpg", "not-a-url")
        coEvery { remote.fetchImages() } returns remoteLines
        every { dao.getAll() } returns flowOf(emptyList())

        val result = repository.refresh()

        assertTrue(result.isSuccess)
        coVerify { 
            dao.insertAll(match { items ->
                items.size == 2 && 
                items[0].isImage && 
                !items[1].isImage &&
                items[0].status == "Pending" &&
                items[1].status == "Ready"
            })
        }
    }

    @Test
    fun `processPending downloads and saves images`() = runTest {
        val pendingEntity = ImageEntity("img_0", "https://test.com/img.jpg", true, null, null, "Pending")
        every { dao.getAll() } returns flowOf(listOf(pendingEntity))
        coEvery { dao.getById("img_0") } returns pendingEntity
        coEvery { downloader.download(any()) } returns Result.success(byteArrayOf(1, 2, 3))
        coEvery { cache.saveOriginal(any(), any()) } returns "orig_path"
        coEvery { cache.savePreview(any(), any()) } returns "thumb_path"

        repository.processPending()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { 
            dao.updateThumbnail("img_0", "thumb_path", "Ready")
            dao.updateOriginal("img_0", "orig_path", "Ready")
        }
    }

    @Test
    fun `processPending handles download error and sets status to Error`() = runTest {
        val pendingEntity = ImageEntity("img_0", "https://test.com/img.jpg", true, null, null, "Pending")
        every { dao.getAll() } returns flowOf(listOf(pendingEntity))
        coEvery { dao.getById("img_0") } returns pendingEntity
        coEvery { downloader.download(any()) } returns Result.failure(Exception("Network error"))

        repository.processPending()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { 
            dao.updateThumbnail("img_0", null, "Error")
        }
    }

    @Test
    fun `retry calls processSingle and updates status`() = runTest {
        val errorEntity = ImageEntity("img_1", "https://test.com/img.jpg", true, null, null, "Error")
        coEvery { dao.getById("img_1") } returns errorEntity
        coEvery { downloader.download(any()) } returns Result.success(byteArrayOf(4, 5))
        coEvery { cache.saveOriginal(any(), any()) } returns "orig2"
        coEvery { cache.savePreview(any(), any()) } returns "thumb2"

        val result = repository.retry("img_1")

        assertTrue(result.isSuccess)
        coVerify { 
            dao.updateThumbnail("img_1", "thumb2", "Ready")
        }
    }
}
