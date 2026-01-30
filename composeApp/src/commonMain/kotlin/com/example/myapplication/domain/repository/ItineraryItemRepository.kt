@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.domain.repository

import com.example.myapplication.domain.entity.ItineraryItem
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * 行程項目資料存取介面
 */
interface ItineraryItemRepository {
    suspend fun addItem(item: ItineraryItem): Result<ItineraryItem>
    suspend fun updateItem(item: ItineraryItem): Result<ItineraryItem>
    suspend fun deleteItem(id: String): Result<Unit>
    suspend fun getItem(id: String): Result<ItineraryItem?>
    suspend fun getItemsByItinerary(itineraryId: String): Result<List<ItineraryItem>>
    suspend fun getItemsByLocation(locationName: String): Result<List<ItineraryItem>>
    suspend fun getItemsByDateRange(start: LocalDate, end: LocalDate): Result<List<ItineraryItem>>
    suspend fun toggleCompletion(itemId: String, currentTimestamp: Instant): Result<ItineraryItem>
    suspend fun calculateProgress(itineraryId: String): Result<Float>

    /**
     * 重新排序行程項目
     * @param itineraryId 行程 ID
     * @param itemIds 新順序的項目 ID 列表
     */
    suspend fun reorderItems(itineraryId: String, itemIds: List<String>): Result<Unit>
}
