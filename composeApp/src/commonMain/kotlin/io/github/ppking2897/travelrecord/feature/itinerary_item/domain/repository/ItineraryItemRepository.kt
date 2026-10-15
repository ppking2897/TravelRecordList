@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository

import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem

/**
 * 行程項目資料存取介面
 */
interface ItineraryItemRepository {
    suspend fun addItem(item: ItineraryItem): Result<ItineraryItem>
    suspend fun updateItem(item: ItineraryItem): Result<ItineraryItem>
    suspend fun deleteItem(id: String): Result<Unit>
    suspend fun getItem(id: String): Result<ItineraryItem?>
    suspend fun getItemsByItinerary(itineraryId: String): Result<List<ItineraryItem>>
}
