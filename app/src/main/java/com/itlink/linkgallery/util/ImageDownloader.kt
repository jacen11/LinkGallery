package com.itlink.linkgallery.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageDownloader @Inject constructor(private val client: OkHttpClient) {
    suspend fun download(url: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url(url).build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext Result.failure(IOException("HTTP ${resp.code}"))
                val bytes = resp.body.bytes()
                Result.success(bytes)
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
}
