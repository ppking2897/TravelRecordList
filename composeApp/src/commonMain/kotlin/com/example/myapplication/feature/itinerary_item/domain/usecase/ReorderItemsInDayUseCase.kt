package com.example.myapplication.feature.itinerary_item.domain.usecase

import com.example.myapplication.feature.itinerary_item.domain.repository.ItineraryItemRepository

/**
 * 重新排序同一日期內的行程項目。
 *
 * 呼叫端負責：
 * - 確保 [itemIds] 裡的所有 item 都屬於同一日期（日內順序才有意義）
 * - [itemIds] 即為期望的新順序（index 0 = 第一項）
 */
class ReorderItemsInDayUseCase(
    private val repository: ItineraryItemRepository
) {
    suspend operator fun invoke(itineraryId: String, itemIds: List<String>): Result<Unit> {
        return repository.reorderItems(itineraryId, itemIds)
    }
}
