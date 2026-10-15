package io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository

import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Photo

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
     * 生成並儲存縮圖（更新 Photo 物件）
     */
    suspend fun generateAndSaveThumbnail(photo: Photo): Result<Photo>
}
