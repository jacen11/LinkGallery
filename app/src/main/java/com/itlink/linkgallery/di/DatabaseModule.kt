package com.itlink.linkgallery.data

import android.content.Context
import androidx.room.Room
import com.itlink.linkgallery.data.local.ImageDao
import com.itlink.linkgallery.data.local.ImageDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideImageDatabase(@ApplicationContext context: Context): ImageDatabase {
        return Room.databaseBuilder(
            context,
            ImageDatabase::class.java,
            "images.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideImageDao(db: ImageDatabase): ImageDao = db.imageDao()
}
