package io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.TravelPhase
import kotlinx.datetime.LocalDate

class GetItineraryPhaseUseCase {
    operator fun invoke(
        itinerary: Itinerary,
        today: LocalDate
    ): TravelPhase {
        val startDate = itinerary.startDate
        val endDate = itinerary.endDate

        return when {
            startDate != null && endDate != null && today in startDate..endDate -> TravelPhase.InProgress
            endDate != null && today > endDate -> TravelPhase.Completed
            else -> TravelPhase.Planning
        }
    }
}
