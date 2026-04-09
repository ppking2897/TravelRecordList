package com.example.myapplication.feature.itinerary.domain.usecase

import com.example.myapplication.feature.itinerary.domain.model.Itinerary
import com.example.myapplication.feature.itinerary.domain.repository.ItineraryRepository

class GetAllItinerariesUseCase(
    private val repository: ItineraryRepository
) {
    suspend operator fun invoke(): Result<List<Itinerary>> {
        return repository.getAllItineraries()
    }
}
