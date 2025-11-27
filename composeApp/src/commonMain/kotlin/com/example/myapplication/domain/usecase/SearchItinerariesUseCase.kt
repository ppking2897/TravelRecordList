package com.example.myapplication.domain.usecase

import com.example.myapplication.data.model.Itinerary
import com.example.myapplication.data.repository.ItineraryRepository

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
