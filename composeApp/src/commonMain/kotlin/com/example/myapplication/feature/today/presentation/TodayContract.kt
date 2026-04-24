package com.example.myapplication.feature.today.presentation

import com.example.myapplication.core.domain.service.LocationSuggestion
import com.example.myapplication.core.presentation.mvi.UiEvent
import com.example.myapplication.core.presentation.mvi.UiIntent
import com.example.myapplication.core.presentation.mvi.UiState
import com.example.myapplication.feature.itinerary.domain.model.Itinerary
import com.example.myapplication.feature.itinerary_item.domain.model.ItineraryItem
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class TodayState(
    val isLoading: Boolean = false,
    val isSavingQuickRecord: Boolean = false,
    val today: LocalDate? = null,
    val currentTrip: Itinerary? = null,
    val todayItems: List<ItineraryItem> = emptyList(),
    val error: String? = null,
    val quickRecord: QuickRecordState = QuickRecordState(),
    val editingNoteItemId: String? = null,
    val editingNoteText: String = ""
) : UiState {
    val completedCount: Int
        get() = todayItems.count { it.isCompleted }

    val progressText: String
        get() = if (todayItems.isEmpty()) {
            "今天還沒有安排"
        } else {
            "$completedCount/${todayItems.size} 已完成"
        }
}

data class QuickRecordState(
    val activity: String = "",
    val locationName: String = "",
    val locationAddress: String? = null,
    val locationLatitude: Double? = null,
    val locationLongitude: Double? = null,
    val notes: String = "",
    val arrivalTime: LocalTime? = null,
    val photoPaths: List<String> = emptyList(),
    val activityError: String? = null,
    val locationError: String? = null
)

sealed class TodayIntent : UiIntent {
    data object LoadToday : TodayIntent()
    data object Refresh : TodayIntent()
    data class UpdateQuickActivity(val value: String) : TodayIntent()
    data class UpdateQuickLocation(val value: String) : TodayIntent()
    data class SelectQuickLocation(val suggestion: LocationSuggestion?) : TodayIntent()
    data class UpdateQuickNotes(val value: String) : TodayIntent()
    data class AddQuickPhoto(val imageData: ByteArray) : TodayIntent() {
        override fun equals(other: Any?): Boolean {
            return other is AddQuickPhoto && imageData.contentEquals(other.imageData)
        }

        override fun hashCode(): Int = imageData.contentHashCode()
    }
    data object SaveQuickRecord : TodayIntent()
    data class ToggleItemCompletion(val itemId: String) : TodayIntent()
    data class AddPhotoToItem(val itemId: String, val imageData: ByteArray) : TodayIntent() {
        override fun equals(other: Any?): Boolean {
            return other is AddPhotoToItem &&
                itemId == other.itemId &&
                imageData.contentEquals(other.imageData)
        }

        override fun hashCode(): Int = 31 * itemId.hashCode() + imageData.contentHashCode()
    }
    data class StartEditingNote(val itemId: String) : TodayIntent()
    data class UpdateEditingNote(val value: String) : TodayIntent()
    data object SaveEditingNote : TodayIntent()
    data object CancelEditingNote : TodayIntent()
}

sealed class TodayEvent : UiEvent {
    data class ShowMessage(val message: String) : TodayEvent()
}
