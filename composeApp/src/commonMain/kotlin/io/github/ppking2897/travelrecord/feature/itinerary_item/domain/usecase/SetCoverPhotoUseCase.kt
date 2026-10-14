package io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase

import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.PhotoRepository

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
