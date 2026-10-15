package io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.TravelPhase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import kotlinx.datetime.LocalDate

/**
 * 找出今天正在進行的旅行
 *
 * 開始與結束日期都有填、且今天落在兩者之間（含頭尾）的行程算進行中；
 * 同時有好幾趟時，取結束日最早的那趟。沒有就回傳 null。
 */
class GetCurrentTripUseCase(
    private val repository: ItineraryRepository
) {
    private val phaseUseCase = GetItineraryPhaseUseCase()

    suspend operator fun invoke(today: LocalDate): Result<Itinerary?> {
        return repository.getAllItineraries().map { itineraries ->
            itineraries
                .filter { phaseUseCase(it, today) == TravelPhase.InProgress }
                .minWithOrNull(compareBy { it.endDate })
        }
    }
}
