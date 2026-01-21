package com.example.myapplication.domain.usecase

import com.example.myapplication.data.model.Photo
import com.example.myapplication.data.repository.PhotoRepository

/**
 * 新增照片 Use Case
 */
class AddPhotoUseCase(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(itemId: String, imageData: ByteArray): Result<Photo> {
        return photoRepository.addPhoto(itemId, imageData)
    }
}
