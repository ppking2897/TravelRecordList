package com.example.myapplication.ui.mvi.additem

import com.example.myapplication.data.model.Itinerary
import com.example.myapplication.ui.mvi.UiEvent
import com.example.myapplication.ui.mvi.UiIntent
import com.example.myapplication.ui.mvi.UiState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * AddEditItem 畫面的 MVI Contract
 */

/**
 * AddEditItem State
 */
data class AddEditItemState(
    val itinerary: Itinerary? = null,
    val activity: String = "",
    val locationName: String = "",
    val locationAddress: String = "",
    val notes: String = "",
    val selectedDate: LocalDate? = null,
    val arrivalTime: LocalTime? = null,
    val departureTime: LocalTime? = null,
    val activityError: String? = null,
    val locationError: String? = null,
    val dateError: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val hasDateRange: Boolean = false,
    val photos: List<String> = emptyList()
) : UiState

/**
 * AddEditItem Intent
 */
sealed class AddEditItemIntent : UiIntent {
    data class LoadItinerary(val itineraryId: String) : AddEditItemIntent()
    data class Initialize(val itinerary: Itinerary) : AddEditItemIntent()
    data class UpdateActivity(val activity: String) : AddEditItemIntent()
    data class UpdateLocationName(val name: String) : AddEditItemIntent()
    data class UpdateLocationAddress(val address: String) : AddEditItemIntent()
    data class UpdateNotes(val notes: String) : AddEditItemIntent()
    data class UpdateDate(val date: LocalDate?) : AddEditItemIntent()
    data class UpdateArrivalTime(val time: LocalTime?) : AddEditItemIntent()
    data class UpdateDepartureTime(val time: LocalTime?) : AddEditItemIntent()
    data class AddPhoto(val path: String) : AddEditItemIntent()
    data class AddPhotoByContent(val content: ByteArray) : AddEditItemIntent()
    data class RemovePhoto(val path: String) : AddEditItemIntent()
    object Save : AddEditItemIntent()
}

/**
 * AddEditItem Event
 */
sealed class AddEditItemEvent : UiEvent {
    object SaveSuccess : AddEditItemEvent()
    data class ShowError(val message: String) : AddEditItemEvent()
}
