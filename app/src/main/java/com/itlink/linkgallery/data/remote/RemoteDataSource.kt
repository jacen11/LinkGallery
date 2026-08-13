package com.itlink.linkgallery.data.remote

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(private val api: com.itlink.linkgallery.data.remote.api.ImageApi) {
    suspend fun fetchImages(): List<String> {
        val body = api.getImages().string()
        return body.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
    }
}
