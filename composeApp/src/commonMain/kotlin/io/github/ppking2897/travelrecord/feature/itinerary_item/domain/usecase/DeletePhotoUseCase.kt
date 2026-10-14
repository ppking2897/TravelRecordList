package com.example.myapplication.feature.itinerary_item.domain.usecase

import com.example.myapplication.feature.itinerary_item.domain.repository.PhotoRepository

/**
 * 刪除照片 Use Case
 */
class DeletePhotoUseCase(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(photoId: String): Result<Unit> {
        return photoRepository.deletePhoto(photoId)
    }
}
