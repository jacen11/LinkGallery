package com.itlink.linkgallery.util

object UriHelper {
    private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
    private val IMAGE_HOST_PATTERNS = listOf(
        "akspic.ru",
        "gstatic.com",
        "googleusercontent.com",
        "wikimedia.org",
        "imgur.com",
        "unsplash.com",
        "pexels.com",
        "pixabay.com",
        "images.unsplash.com",
        "images.pexels.com"
    )

    fun isImageUrl(url: String): Boolean {
        if (!url.startsWith("http://") && !url.startsWith("https://")) return false

        val withoutQuery = url.substringBefore('?')
        val path = withoutQuery.substringAfterLast('/')

        if (path.isEmpty()) return false

        val lower = path.lowercase()
        val lastDot = lower.lastIndexOf('.')
        if (lastDot >= 0) {
            val ext = lower.substring(lastDot + 1)
            if (ext in IMAGE_EXTENSIONS) return true
        }

        val lowerUrl = url.lowercase()
        if (IMAGE_HOST_PATTERNS.any { lowerUrl.contains(it) }) return true

        if (lowerUrl.contains("/images") && lowerUrl.contains("tbn=")) return true

        return false
    }
}
