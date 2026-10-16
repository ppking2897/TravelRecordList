package io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.core.domain.validation.Validation
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * 建立新行程的 Use Case
 */
@OptIn(kotlin.time.ExperimentalTime::class, ExperimentalUuidApi::class)
class CreateItineraryUseCase(
    private val repository: ItineraryRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        startDate: LocalDate?,
        endDate: LocalDate?,
        currentTimestamp: kotlin.time.Instant,
        coverPhotoPath: String? = null
    ): Result<Itinerary> {
        return try {
            // 驗證輸入
            Validation.validateTitle(title).getOrElse { return Result.failure(it) }
            Validation.validateDateRange(startDate, endDate).getOrElse { return Result.failure(it) }

            // 建立 itinerary
            val itinerary = Itinerary(
                id = Uuid.random().toString(),
                title = title,
                description = description,
                startDate = startDate,
                endDate = endDate,
                coverPhotoPath = coverPhotoPath,
                items = emptyList(),
                createdAt = currentTimestamp,
                modifiedAt = currentTimestamp
            )
            
            // 儲存
            repository.createItinerary(itinerary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
