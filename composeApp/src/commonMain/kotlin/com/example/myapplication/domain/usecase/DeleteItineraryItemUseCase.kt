package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.repository.ItineraryItemRepository
import com.example.myapplication.domain.repository.ItineraryRepository
import kotlin.time.ExperimentalTime

/**
 * 刪除行程項目的 Use Case
 */
@ExperimentalTime
class DeleteItineraryItemUseCase(
    private val itemRepository: ItineraryItemRepository,
    private val itineraryRepository: ItineraryRepository
) {
    suspend operator fun invoke(itemId: String): Result<Unit> {
        return try {
            // 1. 取得 Item 以知道它屬於哪個 Itinerary
            val item = itemRepository.getItem(itemId).getOrThrow() ?: return Result.failure(Exception("Item not found"))
            
            // 2. 刪除 Item
            itemRepository.deleteItem(itemId).getOrThrow()
            
            // 3. 更新 Itinerary
            val itinerary = itineraryRepository.getItinerary(item.itineraryId).getOrThrow()
            if (itinerary != null) {
                val updatedItinerary = itinerary.copy(
                    items = itinerary.items.filter { it.id != itemId }
                )
                itineraryRepository.updateItinerary(updatedItinerary).getOrThrow()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
