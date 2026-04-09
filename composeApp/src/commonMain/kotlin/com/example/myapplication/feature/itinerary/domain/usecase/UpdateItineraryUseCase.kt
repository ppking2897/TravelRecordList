package com.example.myapplication.feature.itinerary.domain.usecase

import com.example.myapplication.feature.itinerary.domain.model.Itinerary
import com.example.myapplication.core.domain.validation.Validation
import com.example.myapplication.feature.itinerary.domain.repository.ItineraryRepository
import kotlinx.datetime.Instant

/**
 * 更新行程的 Use Case
 * 
 * 驗證並更新現有行程資料
 */
@OptIn(kotlin.time.ExperimentalTime::class)
class UpdateItineraryUseCase(
    private val repository: ItineraryRepository
) {
    suspend operator fun invoke(
        itinerary: Itinerary,
        currentTimestamp: Instant
    ): Result<Itinerary> {
        return try {
            // 驗證輸入
            Validation.validateItinerary(itinerary).getOrElse { return Result.failure(it) }
            
            // 更新 modifiedAt 時間戳記
            val updatedItinerary = itinerary.copy(modifiedAt = currentTimestamp)
            
            // 儲存
            repository.updateItinerary(updatedItinerary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
