package com.example.myapplication.presentation.travel_history

import com.example.myapplication.domain.entity.ItineraryItem
import com.example.myapplication.presentation.mvi.UiEvent
import com.example.myapplication.presentation.mvi.UiIntent
import com.example.myapplication.presentation.mvi.UiState
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
    val error: String? = null
) : UiState

/**
 * TravelHistory Intent
 */
sealed class TravelHistoryIntent : UiIntent {
    object LoadHistory : TravelHistoryIntent()
    data class FilterByDateRange(val dateRange: ClosedRange<LocalDate>) : TravelHistoryIntent()
    object ClearFilter : TravelHistoryIntent()
}

/**
 * TravelHistory Event
 */
sealed class TravelHistoryEvent : UiEvent {
    object NavigateBack : TravelHistoryEvent()
    object ShowFilterDialog : TravelHistoryEvent()
    data class ShowError(val message: String) : TravelHistoryEvent()
}
