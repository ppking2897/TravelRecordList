package com.example.myapplication.core.itinerary.domain.usecase

import com.example.myapplication.core.itinerary.domain.model.Itinerary
import com.example.myapplication.core.itinerary.domain.repository.ItineraryRepository

class GetItineraryUseCase(
    private val repository: ItineraryRepository
) {
    suspend operator fun invoke(id: String): Result<Itinerary?> {
        return repository.getItinerary(id)
    }
}
