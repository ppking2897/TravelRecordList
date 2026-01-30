package com.example.myapplication.presentation.itinerary_detail

import com.example.myapplication.domain.entity.Itinerary
import com.example.myapplication.domain.entity.ItineraryItem
import com.example.myapplication.presentation.mvi.UiEvent
import com.example.myapplication.presentation.mvi.UiIntent
import com.example.myapplication.presentation.mvi.UiState
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
    val error: String? = null,
    // 拖曳排序相關狀態
    val isDragging: Boolean = false,
    val draggedItemId: String? = null,
    // 批量操作相關狀態
    val isSelectionMode: Boolean = false,
    val selectedItemIds: Set<String> = emptySet()
) : UiState

sealed class ItineraryDetailIntent : UiIntent {
    data class LoadItinerary(val id: String) : ItineraryDetailIntent()
    data class SelectDate(val date: LocalDate?) : ItineraryDetailIntent()
    data class ToggleItemCompletion(val itemId: String) : ItineraryDetailIntent()
    data class DeleteItem(val itemId: String) : ItineraryDetailIntent()
    data class DeleteItinerary(val id: String) : ItineraryDetailIntent()
    object GenerateRoute : ItineraryDetailIntent()
    data class ToggleItemExpansion(val itemId: String) : ItineraryDetailIntent()
    data class AddPhoto(val itemId: String, val imageData: ByteArray) : ItineraryDetailIntent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as AddPhoto

            if (itemId != other.itemId) return false
            if (!imageData.contentEquals(other.imageData)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = itemId.hashCode()
            result = 31 * result + imageData.contentHashCode()
            return result
        }
    }

    data class DeletePhoto(val photoId: String) : ItineraryDetailIntent()
    data class SetCoverPhoto(val itemId: String, val photoId: String) : ItineraryDetailIntent()
    data class FilterByHashtag(val hashtag: String?) : ItineraryDetailIntent()
    data class QuickAddItem(val afterDayIndex: Int) : ItineraryDetailIntent()

    // 拖曳排序相關 Intent
    data class StartDrag(val itemId: String) : ItineraryDetailIntent()
    object EndDrag : ItineraryDetailIntent()
    data class ReorderItems(val fromIndex: Int, val toIndex: Int) : ItineraryDetailIntent()

    // 批量操作相關 Intent
    object ToggleSelectionMode : ItineraryDetailIntent()
    data class ToggleItemSelection(val itemId: String) : ItineraryDetailIntent()
    object SelectAll : ItineraryDetailIntent()
    object ClearSelection : ItineraryDetailIntent()
    object BatchDelete : ItineraryDetailIntent()
    object BatchMarkComplete : ItineraryDetailIntent()
}

sealed class ItineraryDetailEvent : UiEvent {
    object NavigateBack : ItineraryDetailEvent()
    object NavigateToAddItem : ItineraryDetailEvent()
    data class NavigateToQuickAddItem(val defaultDate: LocalDate) : ItineraryDetailEvent()
    data class NavigateToEditItem(val itemId: String) : ItineraryDetailEvent()
    object NavigateToEditItinerary : ItineraryDetailEvent()
    data class ShowDeleteItemConfirm(val item: ItineraryItem) : ItineraryDetailEvent()
    data class ShowDeleteItineraryConfirm(val itinerary: Itinerary) : ItineraryDetailEvent()
    data class NavigateToRoute(val routeId: String) : ItineraryDetailEvent()
    data class ShowError(val message: String) : ItineraryDetailEvent()
    data class ShowPhotoViewer(val photoId: String) : ItineraryDetailEvent()
    data class ShowImagePicker(val itemId: String) : ItineraryDetailEvent()
    data class ShowBatchDeleteConfirm(val count: Int) : ItineraryDetailEvent()
    data class ShowBatchOperationResult(val message: String) : ItineraryDetailEvent()
}
