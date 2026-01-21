package com.example.myapplication.domain.usecase

import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.data.model.Validation
import com.example.myapplication.data.repository.ItineraryItemRepository
import com.example.myapplication.data.repository.ItineraryRepository

/**
 * 更新行程項目的 Use Case
 */
@OptIn(kotlin.time.ExperimentalTime::class)
class UpdateItineraryItemUseCase(
    private val itemRepository: ItineraryItemRepository,
    private val itineraryRepository: ItineraryRepository,
    private val extractHashtagsUseCase: ExtractHashtagsUseCase
) {
    suspend operator fun invoke(
        item: ItineraryItem,
        currentTimestamp: kotlinx.datetime.Instant
    ): Result<ItineraryItem> {
        return try {
            // 驗證輸入
            Validation.validateItineraryItem(item).getOrElse { return Result.failure(it) }
            
            // 提取標籤
            val hashtags = extractHashtagsUseCase(item.notes)
            
            // 更新 modifiedAt 和 hashtags
            val updatedItem = item.copy(
                modifiedAt = currentTimestamp,
                hashtags = hashtags
            )
            
            // 儲存 Item
            itemRepository.updateItem(updatedItem).getOrThrow()
            
            // 同步更新 Itinerary
            val itinerary = itineraryRepository.getItinerary(updatedItem.itineraryId).getOrThrow()
            if (itinerary != null) {
                val updatedItinerary = itinerary.copy(
                    items = itinerary.items.map { if (it.id == updatedItem.id) updatedItem else it }
                )
                itineraryRepository.updateItinerary(updatedItinerary).getOrThrow()
            }
            
            Result.success(updatedItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
