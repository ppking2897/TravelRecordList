package io.github.ppking2897.travelrecord.test

import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.ItineraryItemRepository

/**
 * 只提供「讀取某行程的項目」的 ItineraryItemRepository，給不測資料層的測試用；其他方法不該被呼叫
 */
class StubItineraryItemRepository(
    private val items: List<ItineraryItem>
) : ItineraryItemRepository {
    override suspend fun getItemsByItinerary(itineraryId: String): Result<List<ItineraryItem>> =
        Result.success(items.filter { it.itineraryId == itineraryId })

    override suspend fun addItem(item: ItineraryItem) = error("不應呼叫")
    override suspend fun updateItem(item: ItineraryItem) = error("不應呼叫")
    override suspend fun deleteItem(id: String) = error("不應呼叫")
    override suspend fun getItem(id: String) = error("不應呼叫")
}
