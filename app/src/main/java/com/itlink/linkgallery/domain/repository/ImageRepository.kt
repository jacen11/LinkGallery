package com.itlink.linkgallery.domain.repository

import com.itlink.linkgallery.domain.model.ImageItem
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    fun getImages(): Flow<List<ImageItem>>
    suspend fun refresh(): Result<Unit>
    suspend fun retry(id: String): Result<Unit>
    suspend fun processPending()
}
