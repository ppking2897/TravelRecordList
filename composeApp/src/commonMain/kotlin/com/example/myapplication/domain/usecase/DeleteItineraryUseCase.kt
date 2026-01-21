package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.ItineraryItemRepository
import com.example.myapplication.data.repository.ItineraryRepository

/**
 * 刪除行程的 Use Case
 * 
 * 刪除行程並級聯刪除所有相關項目
 */
@OptIn(kotlin.time.ExperimentalTime::class)
class DeleteItineraryUseCase(
    private val itineraryRepository: ItineraryRepository,
    private val itemRepository: ItineraryItemRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return try {
            // 先獲取行程以確認存在
            val itinerary = itineraryRepository.getItinerary(id).getOrNull()
                ?: return Result.failure(Exception("行程不存在"))
            
            // 刪除所有相關項目
            itinerary.items.forEach { item ->
                itemRepository.deleteItem(item.id)
            }
            
            // 刪除行程
            itineraryRepository.deleteItinerary(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
