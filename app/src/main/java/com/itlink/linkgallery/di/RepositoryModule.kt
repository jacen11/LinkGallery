package com.itlink.linkgallery.di

import com.itlink.linkgallery.data.repository.ImageRepositoryImpl
import com.itlink.linkgallery.domain.repository.ImageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindImageRepository(repo: ImageRepositoryImpl): ImageRepository
}
