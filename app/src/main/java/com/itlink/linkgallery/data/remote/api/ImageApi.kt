package com.itlink.linkgallery.data.remote.api

import okhttp3.ResponseBody
import retrofit2.http.GET

interface ImageApi {
    @GET("images.txt")
    suspend fun getImages(): ResponseBody
}
