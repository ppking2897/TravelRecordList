package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.PhotoRepository

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
