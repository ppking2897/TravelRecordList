package com.example.myapplication.feature.travel_history.presentation

import com.example.myapplication.feature.itinerary_item.domain.model.ItineraryItem
import com.example.myapplication.core.presentation.mvi.UiEvent
import com.example.myapplication.core.presentation.mvi.UiIntent
import com.example.myapplication.core.presentation.mvi.UiState
import kotlinx.datetime.LocalDate

/**
 * TravelHistory 畫面的 MVI Contract
 */

/**
 * TravelHistory State
 */
data class TravelHistoryState(
    val historyByLocation: Map<String, List<ItineraryItem>> = emptyMap(),
    val dateFilter: ClosedRange<LocalDate>? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val expandedItemIds: Set<String> = emptySet()
) : UiState

/**
 * TravelHistory Intent
 */
sealed class TravelHistoryIntent : UiIntent {
    object LoadHistory : TravelHistoryIntent()
    data class FilterByDateRange(val dateRange: ClosedRange<LocalDate>) : TravelHistoryIntent()
    object ClearFilter : TravelHistoryIntent()
    data class ToggleItemExpansion(val itemId: String) : TravelHistoryIntent()
}

/**
 * TravelHistory Event
 */
sealed class TravelHistoryEvent : UiEvent {
    object NavigateBack : TravelHistoryEvent()
    object ShowFilterDialog : TravelHistoryEvent()
    data class ShowError(val message: String) : TravelHistoryEvent()
}
