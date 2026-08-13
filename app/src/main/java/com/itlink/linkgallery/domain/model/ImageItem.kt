package com.itlink.linkgallery.domain.model

data class ImageItem(
    val id: String,
    val url: String,
    val isImage: Boolean,
    val thumbnailPath: String? = null,
    val originalPath: String? = null,
    val status: Status = Status.Pending
) {
    enum class Status { Pending, Ready, Error }
}
