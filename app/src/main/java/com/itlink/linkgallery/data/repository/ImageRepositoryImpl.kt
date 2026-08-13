package com.itlink.linkgallery.data.repository

import com.itlink.linkgallery.data.FileCache
import com.itlink.linkgallery.data.local.ImageDao
import com.itlink.linkgallery.data.local.ImageEntity
import com.itlink.linkgallery.data.remote.RemoteDataSource
import com.itlink.linkgallery.domain.model.ImageItem
import com.itlink.linkgallery.domain.repository.ImageRepository
import com.itlink.linkgallery.util.ImageDownloader
import com.itlink.linkgallery.util.UriHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    private val remote: RemoteDataSource,
    private val dao: ImageDao,
    private val cache: FileCache,
    private val downloader: ImageDownloader,
) : ImageRepository {

    private val downloadSemaphore = Semaphore(10)

    override fun getImages(): Flow<List<ImageItem>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun refresh(): Result<Unit> = try {
        val lines = remote.fetchImages()
        val existingItems = dao.getAll().first().associateBy { it.id }
        val items = lines.mapIndexed { index, raw ->
            val trimmed = raw.trim()
            val isImg = UriHelper.isImageUrl(trimmed)
            val id = "img_$index"

            val existing = existingItems[id]
            val hasCache = existing != null
                && existing.isImage
                && cache.hasPreview(id)
                && cache.hasOriginal(id)

            ImageEntity(
                id = id,
                url = trimmed,
                isImage = isImg,
                thumbnailPath = if (hasCache) existing.thumbnailPath else null,
                originalPath = if (hasCache) existing.originalPath else null,
                status = when {
                    !isImg -> "Ready"
                    hasCache -> "Ready"
                    else -> "Pending"
                }
            )
        }
        dao.insertAll(items)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun retry(id: String): Result<Unit> = try {
        withContext(Dispatchers.IO) {
            processSingle(id)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun processPending() = coroutineScope {
        val list = dao.getAll().first()
        val pending = list.filter { it.status == "Pending" }
        
        pending.forEach { entity ->
            launch {
                downloadSemaphore.withPermit {
                    processSingle(entity.id)
                }
            }
        }
    }

    private suspend fun processSingle(id: String) {
        val entity = dao.getById(id) ?: return
        if (!entity.isImage) {
            dao.updateThumbnail(id, null, "Ready")
            return
        }

        val downloadResult = downloader.download(entity.url)
        if (downloadResult.isSuccess) {
            val bytes = downloadResult.getOrNull()!!
            val orig = cache.saveOriginal(id, bytes)
            val thumb = cache.savePreview(id, bytes)
            val status = if (orig != null && thumb != null) "Ready" else "Error"
            dao.updateThumbnail(id, thumb, status)
            dao.updateOriginal(id, orig, status)
        } else {
            val fallbackUrl = entity.url.replaceFirst("https://", "http://")
            if (fallbackUrl != entity.url) {
                val retryResult = downloader.download(fallbackUrl)
                if (retryResult.isSuccess) {
                    val bytes = retryResult.getOrNull() ?: throw IllegalStateException()
                    val orig = cache.saveOriginal(id, bytes)
                    val thumb = cache.savePreview(id, bytes)
                    val status = if (orig != null && thumb != null) "Ready" else "Error"
                    dao.updateThumbnail(id, thumb, status)
                    dao.updateOriginal(id, orig, status)
                    return
                }
            }
            dao.updateThumbnail(id, null, "Error")
        }
    }

    private fun ImageEntity.toDomain(): ImageItem = ImageItem(
        id = id,
        url = url,
        isImage = isImage,
        thumbnailPath = thumbnailPath,
        originalPath = originalPath,
        status = when (status) {
            "Ready" -> ImageItem.Status.Ready
            "Error" -> ImageItem.Status.Error
            else -> ImageItem.Status.Pending
        }
    )
}
