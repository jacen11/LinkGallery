package com.itlink.linkgallery.domain.usecase

import com.itlink.linkgallery.domain.model.ImageItem
import com.itlink.linkgallery.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class GetImagesUseCase @Inject constructor(private val repository: ImageRepository) {
    open operator fun invoke(): Flow<List<ImageItem>> = repository.getImages()
}

open class RefreshImagesUseCase @Inject constructor(private val repository: ImageRepository) {
    open suspend operator fun invoke() = repository.refresh()
}

open class RetryImageUseCase @Inject constructor(private val repository: ImageRepository) {
    open suspend operator fun invoke(id: String) = repository.retry(id)
}

open class ProcessPendingUseCase @Inject constructor(private val repository: ImageRepository) {
    open suspend operator fun invoke() = repository.processPending()
}
