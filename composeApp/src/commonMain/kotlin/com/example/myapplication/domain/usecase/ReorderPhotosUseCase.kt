package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.PhotoRepository

/**
 * 重新排序照片 Use Case
 */
class ReorderPhotosUseCase(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(itemId: String, photoIds: List<String>): Result<Unit> {
        return photoRepository.reorderPhotos(itemId, photoIds)
    }
}
