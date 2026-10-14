package io.github.ppking2897.travelrecord.feature.itinerary_add_edit.presentation

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import kotlinx.datetime.LocalDate

/**
 * ItineraryAddEdit 畫面的 MVI Contract
 */

/**
 * ItineraryAddEdit State
 */
data class ItineraryAddEditState(
    val itinerary: Itinerary? = null,
    val title: String = "",
    val description: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val coverPhotoPath: String? = null,
    val titleError: String? = null,
    val dateError: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val showDraftSaved: Boolean = false
)

/**
 * ItineraryAddEdit Intent
 */
sealed class ItineraryAddEditIntent {
    data class LoadItinerary(val itineraryId: String) : ItineraryAddEditIntent()
    object LoadDraft : ItineraryAddEditIntent()
    data class UpdateTitle(val title: String) : ItineraryAddEditIntent()
    data class UpdateDescription(val description: String) : ItineraryAddEditIntent()
    data class UpdateStartDate(val date: LocalDate?) : ItineraryAddEditIntent()
    data class UpdateEndDate(val date: LocalDate?) : ItineraryAddEditIntent()
    data class SetCoverPhoto(val imageData: ByteArray) : ItineraryAddEditIntent()
    object RemoveCoverPhoto : ItineraryAddEditIntent()
    object Save : ItineraryAddEditIntent()
    object SaveDraft : ItineraryAddEditIntent()
}

/**
 * ItineraryAddEdit Event
 */
sealed class ItineraryAddEditEvent {
    data class SaveSuccess(val itineraryId: String) : ItineraryAddEditEvent()
    data class ShowError(val message: String) : ItineraryAddEditEvent()
    object DraftSaved : ItineraryAddEditEvent()
}
