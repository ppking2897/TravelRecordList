@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.feature.footprint.domain.usecase

import io.github.ppking2897.travelrecord.feature.footprint.domain.model.VisitedPlace
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetAllItinerariesUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.GetItemsByItineraryUseCase
import kotlinx.datetime.LocalDate

/**
 * 取得去過的地點：已打卡，或日期在今天以前的項目；今天還沒打卡的不算
 */
class GetVisitedPlacesUseCase(
    private val getAllItinerariesUseCase: GetAllItinerariesUseCase,
    private val getItemsByItineraryUseCase: GetItemsByItineraryUseCase
) {
    suspend operator fun invoke(today: LocalDate): Result<List<VisitedPlace>> = runCatching {
        val itineraries = getAllItinerariesUseCase().getOrThrow()

        itineraries.flatMap { itinerary ->
            val items = getItemsByItineraryUseCase(itinerary.id).getOrElse { emptyList() }
            items.filter { it.isCompleted || it.date < today }.map { item ->
                VisitedPlace(
                    itemId = item.id,
                    locationName = item.location.name,
                    activity = item.activity,
                    date = item.date,
                    tripId = itinerary.id,
                    tripTitle = itinerary.title,
                    latitude = item.location.latitude,
                    longitude = item.location.longitude,
                    isCompleted = item.isCompleted
                )
            }
        }.sortedByDescending { it.date }
    }
}
