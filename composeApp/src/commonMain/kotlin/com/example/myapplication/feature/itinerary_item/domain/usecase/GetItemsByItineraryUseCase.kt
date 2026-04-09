package com.example.myapplication.feature.itinerary_item.domain.usecase

import com.example.myapplication.feature.itinerary_item.domain.model.ItineraryItem
import com.example.myapplication.feature.itinerary_item.domain.repository.ItineraryItemRepository

class GetItemsByItineraryUseCase(
    private val repository: ItineraryItemRepository
) {
    suspend operator fun invoke(itineraryId: String): Result<List<ItineraryItem>> {
        return repository.getItemsByItinerary(itineraryId)
    }
}
