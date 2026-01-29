package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.entity.Itinerary
import com.example.myapplication.domain.repository.ItineraryRepository

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
