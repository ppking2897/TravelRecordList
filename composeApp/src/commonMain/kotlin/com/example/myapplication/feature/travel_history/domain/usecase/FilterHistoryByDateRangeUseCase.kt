package com.example.myapplication.feature.travel_history.domain.usecase

import com.example.myapplication.feature.itinerary_item.domain.model.ItineraryItem
import com.example.myapplication.feature.itinerary_item.domain.repository.ItineraryItemRepository
import kotlinx.datetime.LocalDate

class FilterHistoryByDateRangeUseCase(
    private val repository: ItineraryItemRepository
) {
    suspend operator fun invoke(start: LocalDate, end: LocalDate): Result<Map<String, List<ItineraryItem>>> {
        return repository.getItemsByDateRange(start, end).map { items ->
            val completedItems = items.filter { it.isCompleted }
            val grouped = completedItems.groupBy { it.location.name }
            grouped.mapValues { (_, locationItems) ->
                locationItems.sortedWith(
                    compareBy<ItineraryItem> { it.date }.thenBy { it.primaryTime() }
                )
            }.entries.sortedBy { it.key }.associate { it.key to it.value }
        }
    }
}
