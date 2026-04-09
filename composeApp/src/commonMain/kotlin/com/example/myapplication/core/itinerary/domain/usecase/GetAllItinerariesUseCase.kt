package com.example.myapplication.core.itinerary.domain.usecase

import com.example.myapplication.core.itinerary.domain.model.Itinerary
import com.example.myapplication.core.itinerary.domain.repository.ItineraryRepository

class GetAllItinerariesUseCase(
    private val repository: ItineraryRepository
) {
    suspend operator fun invoke(): Result<List<Itinerary>> {
        return repository.getAllItineraries()
    }
}
