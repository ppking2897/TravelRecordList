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
    val expandedItemIds: Set<String> = emptySet(),
    val selectedHashtag: String? = null,
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
    data class ToggleItemExpansion(val itemId: String) : ItineraryDetailIntent()
    data class AddPhoto(val itemId: String, val imageData: ByteArray) : ItineraryDetailIntent()
    data class DeletePhoto(val photoId: String) : ItineraryDetailIntent()
    data class SetCoverPhoto(val itemId: String, val photoId: String) : ItineraryDetailIntent()
    data class FilterByHashtag(val hashtag: String?) : ItineraryDetailIntent()
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
    data class ShowPhotoViewer(val photoId: String) : ItineraryDetailEvent()
    data class ShowImagePicker(val itemId: String) : ItineraryDetailEvent()
    data class PhotoAdded(val itemId: String) : ItineraryDetailEvent()
    data class PhotoDeleted(val photoId: String) : ItineraryDetailEvent()
}
