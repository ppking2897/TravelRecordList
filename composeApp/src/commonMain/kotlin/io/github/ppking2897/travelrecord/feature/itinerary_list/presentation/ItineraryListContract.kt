package io.github.ppking2897.travelrecord.feature.itinerary_list.presentation

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.core.presentation.mvi.UiEvent
import io.github.ppking2897.travelrecord.core.presentation.mvi.UiIntent
import io.github.ppking2897.travelrecord.core.presentation.mvi.UiState

/**
 * ItineraryList 畫面的 MVI Contract
 */

/**
 * ItineraryList State
 */
data class ItineraryListState(
    val itineraries: List<Itinerary> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) : UiState

/**
 * ItineraryList Intent
 */
sealed class ItineraryListIntent : UiIntent {
    object InitializeAndLoad : ItineraryListIntent()
    object LoadItineraries : ItineraryListIntent()
    object Refresh : ItineraryListIntent()
    data class Search(val query: String) : ItineraryListIntent()
    data class DeleteItinerary(val id: String) : ItineraryListIntent()
}

/**
 * ItineraryList Event
 */
sealed class ItineraryListEvent : UiEvent {
    data class NavigateToDetail(val id: String) : ItineraryListEvent()
    data class NavigateToEdit(val id: String) : ItineraryListEvent()
    object NavigateToAdd : ItineraryListEvent()
    data class ShowDeleteConfirm(val itinerary: Itinerary) : ItineraryListEvent()
    data class ShowError(val message: String) : ItineraryListEvent()
}
