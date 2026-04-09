package com.example.myapplication.core.itinerary.domain.usecase

import com.example.myapplication.core.itinerary.domain.model.Itinerary
import com.example.myapplication.core.itinerary.domain.repository.ItineraryRepository

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
