package com.example.myapplication.feature.itinerary_item.domain.usecase

import com.example.myapplication.feature.itinerary_item.domain.model.Photo
import com.example.myapplication.feature.itinerary_item.domain.repository.PhotoRepository

/**
 * 生成縮圖 Use Case
 * 用於在背景生成照片縮圖並更新資料庫
 */
class GenerateThumbnailUseCase(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(photo: Photo): Result<Photo> {
        return photoRepository.generateAndSaveThumbnail(photo)
    }
}
