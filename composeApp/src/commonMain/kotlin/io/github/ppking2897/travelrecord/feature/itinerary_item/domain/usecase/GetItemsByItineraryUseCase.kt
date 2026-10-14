package io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase

import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.ItineraryItemRepository

class GetItemsByItineraryUseCase(
    private val repository: ItineraryItemRepository
) {
    suspend operator fun invoke(itineraryId: String): Result<List<ItineraryItem>> {
        return repository.getItemsByItinerary(itineraryId)
    }
}
