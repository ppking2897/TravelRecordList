package io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase

import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Photo
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.PhotoRepository

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
