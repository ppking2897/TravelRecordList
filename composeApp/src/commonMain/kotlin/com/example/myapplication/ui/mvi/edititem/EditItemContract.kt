package com.example.myapplication.ui.mvi.edititem

import com.example.myapplication.data.model.Itinerary
import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.ui.mvi.UiEvent
import com.example.myapplication.ui.mvi.UiIntent
import com.example.myapplication.ui.mvi.UiState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * EditItem 畫面的 MVI Contract
 */

/**
 * EditItem State
 */
data class EditItemState(
    val item: ItineraryItem? = null,
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
    val hasDateRange: Boolean = false
) : UiState

/**
 * EditItem Intent
 */
sealed class EditItemIntent : UiIntent {
    data class Initialize(val item: ItineraryItem, val itinerary: Itinerary) : EditItemIntent()
    data class UpdateActivity(val activity: String) : EditItemIntent()
    data class UpdateLocationName(val name: String) : EditItemIntent()
    data class UpdateLocationAddress(val address: String) : EditItemIntent()
    data class UpdateNotes(val notes: String) : EditItemIntent()
    data class UpdateDate(val date: LocalDate?) : EditItemIntent()
    data class UpdateArrivalTime(val time: LocalTime?) : EditItemIntent()
    data class UpdateDepartureTime(val time: LocalTime?) : EditItemIntent()
    object Save : EditItemIntent()
}

/**
 * EditItem Event
 */
sealed class EditItemEvent : UiEvent {
    object SaveSuccess : EditItemEvent()
    data class ShowError(val message: String) : EditItemEvent()
}
