package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.entity.ItineraryItem
import com.example.myapplication.domain.entity.Location
import com.example.myapplication.domain.entity.Photo
import com.example.myapplication.domain.validation.Validation
import com.example.myapplication.domain.repository.ItineraryItemRepository
import com.example.myapplication.domain.repository.ItineraryRepository
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
        photoPaths: List<String> = emptyList(),
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
            
            val itemId = Uuid.random().toString()
            
            // 建立照片物件
            val photos = photoPaths.mapIndexed { index, path ->
                Photo(
                    id = Uuid.random().toString(),
                    itemId = itemId,
                    fileName = path.substringAfterLast('/'),
                    filePath = path,
                    order = index,
                    fileSize = 0L,
                    uploadedAt = currentTimestamp,
                    modifiedAt = currentTimestamp
                )
            }
            
            // 建立 item
            val item = ItineraryItem(
                id = itemId,
                itineraryId = itineraryId,
                date = date,
                arrivalTime = arrivalTime,
                departureTime = departureTime,
                location = location,
                activity = activity,
                notes = notes,
                hashtags = hashtags,
                photos = photos,
                isCompleted = false,
                completedAt = null,
                createdAt = currentTimestamp,
                modifiedAt = currentTimestamp
            )
            
            // 儲存 item
            itemRepository.addItem(item).getOrThrow()
            
            // 同步更新 Itinerary (因為 Itinerary 內含 items 列表)
            val updatedItinerary = itinerary.copy(
                items = itinerary.items + item,
                modifiedAt = currentTimestamp
            )
            itineraryRepository.updateItinerary(updatedItinerary).getOrThrow()
            
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
