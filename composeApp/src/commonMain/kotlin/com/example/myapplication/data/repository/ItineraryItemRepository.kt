package com.example.myapplication.data.repository

import com.example.myapplication.data.model.ItineraryItem
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime

@ExperimentalTime
interface ItineraryItemRepository {
    suspend fun addItem(item: ItineraryItem): Result<ItineraryItem>
    suspend fun updateItem(item: ItineraryItem): Result<ItineraryItem>
    suspend fun deleteItem(id: String): Result<Unit>
    suspend fun getItem(id: String): Result<ItineraryItem?>
    suspend fun getItemsByItinerary(itineraryId: String): Result<List<ItineraryItem>>
    suspend fun getItemsByLocation(locationName: String): Result<List<ItineraryItem>>
    suspend fun getItemsByDateRange(start: LocalDate, end: LocalDate): Result<List<ItineraryItem>>
    suspend fun toggleCompletion(itemId: String, currentTimestamp: kotlinx.datetime.Instant): Result<ItineraryItem>
    suspend fun calculateProgress(itineraryId: String): Result<Float>
}
