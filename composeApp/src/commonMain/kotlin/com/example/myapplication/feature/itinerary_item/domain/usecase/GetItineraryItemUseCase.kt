package com.example.myapplication.feature.itinerary_item.domain.usecase

import com.example.myapplication.feature.itinerary_item.domain.model.ItineraryItem
import com.example.myapplication.feature.itinerary_item.domain.repository.ItineraryItemRepository

class GetItineraryItemUseCase(
    private val repository: ItineraryItemRepository
) {
    suspend operator fun invoke(itemId: String): Result<ItineraryItem?> {
        return repository.getItem(itemId)
    }
}
