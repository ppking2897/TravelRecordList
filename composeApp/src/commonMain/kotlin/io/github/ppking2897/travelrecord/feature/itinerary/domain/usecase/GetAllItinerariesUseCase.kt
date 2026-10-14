package io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository

class GetAllItinerariesUseCase(
    private val repository: ItineraryRepository
) {
    suspend operator fun invoke(): Result<List<Itinerary>> {
        return repository.getAllItineraries()
    }
}
