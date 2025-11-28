package com.example.myapplication.ui.mvi.detail

import com.example.myapplication.data.model.Itinerary
import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.ui.mvi.UiEvent
import com.example.myapplication.ui.mvi.UiIntent
import com.example.myapplication.ui.mvi.UiState
import kotlinx.datetime.LocalDate

data class ItemsByDate(
    val date: LocalDate,
    val items: List<ItineraryItem>
)

data class ItineraryDetailState(
    val itinerary: Itinerary? = null,
    val groupedItems: List<ItemsByDate> = emptyList(),
    val selectedDate: LocalDate? = null,
    val dateRange: ClosedRange<LocalDate>? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

sealed class ItineraryDetailIntent : UiIntent {
    data class LoadItinerary(val id: String) : ItineraryDetailIntent()
    data class SelectDate(val date: LocalDate?) : ItineraryDetailIntent()
    data class ToggleItemCompletion(val itemId: String) : ItineraryDetailIntent()
    data class DeleteItem(val itemId: String) : ItineraryDetailIntent()
    data class DeleteItinerary(val id: String) : ItineraryDetailIntent()
    object GenerateRoute : ItineraryDetailIntent()
}

sealed class ItineraryDetailEvent : UiEvent {
    object NavigateBack : ItineraryDetailEvent()
    object NavigateToAddItem : ItineraryDetailEvent()
    data class NavigateToEditItem(val itemId: String) : ItineraryDetailEvent()
    object NavigateToEditItinerary : ItineraryDetailEvent()
    data class ShowDeleteItemConfirm(val item: ItineraryItem) : ItineraryDetailEvent()
    data class ShowDeleteItineraryConfirm(val itinerary: Itinerary) : ItineraryDetailEvent()
    data class NavigateToRoute(val routeId: String) : ItineraryDetailEvent()
    data class ShowError(val message: String) : ItineraryDetailEvent()
}
