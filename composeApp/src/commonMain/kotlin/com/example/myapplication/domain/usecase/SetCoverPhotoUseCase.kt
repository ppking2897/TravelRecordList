package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.PhotoRepository

/**
 * 設定封面照片 Use Case
 */
class SetCoverPhotoUseCase(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(itemId: String, photoId: String): Result<Unit> {
        return photoRepository.setCoverPhoto(itemId, photoId)
    }
}
