package io.github.ppking2897.travelrecord.feature.itinerary_item.presentation

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.core.domain.service.LocationSuggestion
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
    val locationLatitude: Double? = null,
    val locationLongitude: Double? = null,
    val locationPlaceId: String? = null,
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
) {
    /** 離開時要不要跳「捨棄變更？」：跟讀進來的 [item] 比，任一欄不同就算 */
    val hasUnsavedChanges: Boolean
        get() {
            val original = item ?: return false
            return activity != original.activity ||
                locationName != original.location.name ||
                // 讀進來時沒有地址會填成空字串，比的時候也要一樣
                locationAddress != (original.location.address ?: "") ||
                locationLatitude != original.location.latitude ||
                locationLongitude != original.location.longitude ||
                notes != original.notes ||
                selectedDate != original.date ||
                arrivalTime != original.arrivalTime ||
                departureTime != original.departureTime ||
                photos != original.photos.map { it.filePath }
        }
}

/**
 * EditItem Intent
 */
sealed class EditItemIntent {
    data class LoadItem(val itemId: String) : EditItemIntent()
    data class Initialize(val item: ItineraryItem, val itinerary: Itinerary) : EditItemIntent()
    data class UpdateActivity(val activity: String) : EditItemIntent()
    data class UpdateLocationName(val name: String) : EditItemIntent()
    data class UpdateLocationAddress(val address: String) : EditItemIntent()
    data class SelectLocation(val suggestion: LocationSuggestion?) : EditItemIntent()
    data class UpdateNotes(val notes: String) : EditItemIntent()
    data class UpdateDate(val date: LocalDate?) : EditItemIntent()
    data class UpdateArrivalTime(val time: LocalTime?) : EditItemIntent()
    data class UpdateDepartureTime(val time: LocalTime?) : EditItemIntent()
    data class AddPhoto(val path: String) : EditItemIntent()
    data class AddPhotoByContent(val content: ByteArray) : EditItemIntent()
    data class RemovePhoto(val path: String) : EditItemIntent()
    object Save : EditItemIntent()
}

/**
 * EditItem Event
 */
sealed class EditItemEvent {
    object SaveSuccess : EditItemEvent()
    data class ShowError(val message: String) : EditItemEvent()
}
