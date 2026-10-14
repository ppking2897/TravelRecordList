package io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository

/**
 * 搜尋行程的 Use Case
 */
class SearchItinerariesUseCase(
    private val itineraryRepository: ItineraryRepository
) {
    suspend operator fun invoke(query: String): Result<List<Itinerary>> {
        return itineraryRepository.searchItineraries(query)
    }
}
