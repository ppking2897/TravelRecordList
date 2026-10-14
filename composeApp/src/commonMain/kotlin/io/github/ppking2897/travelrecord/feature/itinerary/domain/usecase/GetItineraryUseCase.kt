package io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository

class GetItineraryUseCase(
    private val repository: ItineraryRepository
) {
    suspend operator fun invoke(id: String): Result<Itinerary?> {
        return repository.getItinerary(id)
    }
}
