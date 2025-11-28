package com.example.myapplication.domain.usecase

import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.data.model.Location
import com.example.myapplication.data.model.Validation
import com.example.myapplication.data.repository.ItineraryItemRepository
import com.example.myapplication.data.repository.ItineraryRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * 新增行程項目的 Use Case
 */
@OptIn(kotlin.time.ExperimentalTime::class, ExperimentalUuidApi::class)
class AddItineraryItemUseCase(
    private val itemRepository: ItineraryItemRepository,
    private val itineraryRepository: ItineraryRepository,
    private val extractHashtagsUseCase: ExtractHashtagsUseCase
) {
    suspend operator fun invoke(
        itineraryId: String,
        date: LocalDate,
        arrivalTime: LocalTime? = null,
        departureTime: LocalTime? = null,
        location: Location,
        activity: String,
        notes: String,
        currentTimestamp: kotlinx.datetime.Instant
    ): Result<ItineraryItem> {
        return try {
            // 驗證 itinerary 存在
            val itinerary = itineraryRepository.getItinerary(itineraryId).getOrNull()
                ?: return Result.failure(Exception("Itinerary not found: $itineraryId"))
            
            // 驗證輸入
            Validation.validateLocation(location).getOrElse { return Result.failure(it) }
            
            if (activity.isBlank()) {
                return Result.failure(Exception("Activity description must not be empty"))
            }
            
            // 驗證時間邏輯
            if (arrivalTime != null && departureTime != null && departureTime < arrivalTime) {
                return Result.failure(Exception("Departure time must be after arrival time"))
            }
            
            // 提取標籤
            val hashtags = extractHashtagsUseCase(notes)
            
            // 建立 item
            val item = ItineraryItem(
                id = Uuid.random().toString(),
                itineraryId = itineraryId,
                date = date,
                arrivalTime = arrivalTime,
                departureTime = departureTime,
                location = location,
                activity = activity,
                notes = notes,
                hashtags = hashtags,
                isCompleted = false,
                completedAt = null,
                photoReferences = emptyList(),
                createdAt = currentTimestamp,
                modifiedAt = currentTimestamp
            )
            
            // 儲存
            itemRepository.addItem(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
