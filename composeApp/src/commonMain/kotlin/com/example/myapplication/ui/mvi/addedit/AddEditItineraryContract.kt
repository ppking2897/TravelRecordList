package com.example.myapplication.ui.mvi.addedit

import com.example.myapplication.data.model.Itinerary
import com.example.myapplication.ui.mvi.UiEvent
import com.example.myapplication.ui.mvi.UiIntent
import com.example.myapplication.ui.mvi.UiState
import kotlinx.datetime.LocalDate

/**
 * AddEditItinerary 畫面的 MVI Contract
 */

/**
 * AddEditItinerary State
 */
data class AddEditItineraryState(
    val itinerary: Itinerary? = null,
    val title: String = "",
    val description: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val titleError: String? = null,
    val dateError: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val showDraftSaved: Boolean = false
) : UiState

/**
 * AddEditItinerary Intent
 */
sealed class AddEditItineraryIntent : UiIntent {
    data class LoadItinerary(val itineraryId: String) : AddEditItineraryIntent()
    object LoadDraft : AddEditItineraryIntent()
    data class UpdateTitle(val title: String) : AddEditItineraryIntent()
    data class UpdateDescription(val description: String) : AddEditItineraryIntent()
    data class UpdateStartDate(val date: LocalDate?) : AddEditItineraryIntent()
    data class UpdateEndDate(val date: LocalDate?) : AddEditItineraryIntent()
    object Save : AddEditItineraryIntent()
    object SaveDraft : AddEditItineraryIntent()
}

/**
 * AddEditItinerary Event
 */
sealed class AddEditItineraryEvent : UiEvent {
    data class SaveSuccess(val itineraryId: String) : AddEditItineraryEvent()
    data class ShowError(val message: String) : AddEditItineraryEvent()
    object DraftSaved : AddEditItineraryEvent()
}
