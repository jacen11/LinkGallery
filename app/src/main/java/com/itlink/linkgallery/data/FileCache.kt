package com.itlink.linkgallery.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileCache @Inject constructor(private val context: Context) {
    private val root = File(context.filesDir, "images").apply { mkdirs() }

    fun previewFile(id: String) = File(root, "preview_$id")
    fun originalFile(id: String) = File(root, "original_$id")

    fun hasPreview(id: String): Boolean = previewFile(id).exists()
    fun hasOriginal(id: String): Boolean = originalFile(id).exists()

    suspend fun savePreview(id: String, bytes: ByteArray, maxDim: Int = 120): String? = withContext(Dispatchers.IO) {
        try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            var sample = 1
            while (opts.outWidth / sample > maxDim || opts.outHeight / sample > maxDim) sample *= 2
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts) ?: return@withContext null
            val out = FileOutputStream(previewFile(id))
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.close()
            bmp.recycle()
            previewFile(id).absolutePath
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveOriginal(id: String, bytes: ByteArray): String? = withContext(Dispatchers.IO) {
        try {
            val out = FileOutputStream(originalFile(id))
            out.write(bytes)
            out.close()
            originalFile(id).absolutePath
        } catch (e: Exception) {
            null
        }
    }

    suspend fun readPreview(id: String): ByteArray? = withContext(Dispatchers.IO) {
        try { previewFile(id).readBytes() } catch (e: Exception) { null }
    }

    suspend fun readOriginal(id: String): ByteArray? = withContext(Dispatchers.IO) {
        try { originalFile(id).readBytes() } catch (e: Exception) { null }
    }
}
