@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.feature.footprint.domain.usecase

import io.github.ppking2897.travelrecord.feature.footprint.domain.model.VisitedPlace
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetAllItinerariesUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.GetItemsByItineraryUseCase

class GetVisitedPlacesUseCase(
    private val getAllItinerariesUseCase: GetAllItinerariesUseCase,
    private val getItemsByItineraryUseCase: GetItemsByItineraryUseCase
) {
    suspend operator fun invoke(): Result<List<VisitedPlace>> = runCatching {
        val itineraries = getAllItinerariesUseCase().getOrThrow()

        itineraries.flatMap { itinerary ->
            val items = getItemsByItineraryUseCase(itinerary.id).getOrElse { emptyList() }
            items.map { item ->
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
