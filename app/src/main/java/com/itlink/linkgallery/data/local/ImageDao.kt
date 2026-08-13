package com.itlink.linkgallery.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Query("SELECT * FROM images")
    fun getAll(): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE id = :id")
    suspend fun getById(id: String): ImageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ImageEntity>)

    @Query("UPDATE images SET thumbnail_path = :path, status = :status WHERE id = :id")
    suspend fun updateThumbnail(id: String, path: String?, status: String)

    @Query("UPDATE images SET original_path = :path, status = :status WHERE id = :id")
    suspend fun updateOriginal(id: String, path: String?, status: String)
}
