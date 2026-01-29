package com.example.myapplication.domain.repository

import com.example.myapplication.domain.entity.Photo

/**
 * 照片 Repository 介面
 */
interface PhotoRepository {
    /**
     * 新增照片到行程項目
     */
    suspend fun addPhoto(itemId: String, imageData: ByteArray): Result<Photo>

    /**
     * 刪除照片
     */
    suspend fun deletePhoto(photoId: String): Result<Unit>

    /**
     * 設定封面照片
     */
    suspend fun setCoverPhoto(itemId: String, photoId: String): Result<Unit>

    /**
     * 重新排序照片
     */
    suspend fun reorderPhotos(itemId: String, photoIds: List<String>): Result<Unit>

    /**
     * 取得行程項目的所有照片
     */
    suspend fun getPhotosByItem(itemId: String): Result<List<Photo>>

    /**
     * 載入照片資料
     */
    suspend fun loadPhotoData(photoPath: String): Result<ByteArray>

    /**
     * 生成並儲存縮圖（更新 Photo 物件）
     */
    suspend fun generateAndSaveThumbnail(photo: Photo): Result<Photo>
}
