package com.itlink.linkgallery.data

import android.content.Context
import com.itlink.linkgallery.util.ImageDownloader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideImageDownloader(client: OkHttpClient): ImageDownloader {
        return ImageDownloader(client)
    }

    @Provides
    @Singleton
    fun provideFileCache(@ApplicationContext context: Context): FileCache {
        return FileCache(context)
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitorImpl(context)
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStoreImpl(context)
    }
}
