package com.example.myapplication.domain.usecase

import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.data.repository.ItineraryItemRepositoryImpl

/**
 * 新增照片到行程項目的 Use Case
 */
@OptIn(kotlin.time.ExperimentalTime::class)
class AddPhotoToItemUseCase(
    private val itemRepository: ItineraryItemRepositoryImpl
) {
    suspend operator fun invoke(
        itemId: String,
        photoReference: String,
        currentTimestamp: kotlinx.datetime.Instant
    ): Result<ItineraryItem> {
        return try {
            if (photoReference.isBlank()) {
                return Result.failure(Exception("Photo reference must not be empty"))
            }
            
            itemRepository.addPhotoReference(itemId, photoReference, currentTimestamp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
