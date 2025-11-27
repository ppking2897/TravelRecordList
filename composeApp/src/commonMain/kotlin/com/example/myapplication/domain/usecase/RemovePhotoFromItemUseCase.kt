package com.example.myapplication.domain.usecase

import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.data.repository.ItineraryItemRepositoryImpl

/**
 * 從行程項目移除照片的 Use Case
 */
@OptIn(kotlin.time.ExperimentalTime::class)
class RemovePhotoFromItemUseCase(
    private val itemRepository: ItineraryItemRepositoryImpl
) {
    suspend operator fun invoke(
        itemId: String,
        photoReference: String,
        currentTimestamp: kotlinx.datetime.Instant
    ): Result<ItineraryItem> {
        return itemRepository.removePhotoReference(itemId, photoReference, currentTimestamp)
    }
}
