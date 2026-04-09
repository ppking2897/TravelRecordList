package com.example.myapplication.feature.itinerary_map.presentation

import com.example.myapplication.feature.itinerary.domain.model.Itinerary
import com.example.myapplication.feature.route.domain.model.MapMarker
import com.example.myapplication.core.presentation.mvi.UiEvent
import com.example.myapplication.core.presentation.mvi.UiIntent
import com.example.myapplication.core.presentation.mvi.UiState
import kotlinx.datetime.LocalDate

/**
 * 地圖畫面的 UI 狀態
 */
data class ItineraryMapState(
    val itinerary: Itinerary? = null,
    val markers: List<MapMarker> = emptyList(),
    val selectedMarker: MapMarker? = null,
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasNoMarkersToShow: Boolean = false
) : UiState

/**
 * 地圖畫面的使用者意圖
 */
sealed class ItineraryMapIntent : UiIntent {
    data class LoadMapData(val itineraryId: String) : ItineraryMapIntent()
    data class SelectMarker(val marker: MapMarker?) : ItineraryMapIntent()
    data class FilterByDate(val date: LocalDate?) : ItineraryMapIntent()
    object DismissError : ItineraryMapIntent()
}

/**
 * 地圖畫面的單次事件
 */
sealed class ItineraryMapEvent : UiEvent {
    object NavigateBack : ItineraryMapEvent()
    data class NavigateToEditItem(val itemId: String) : ItineraryMapEvent()
    data class ShowError(val message: String) : ItineraryMapEvent()
    data class CenterOnMarker(val marker: MapMarker) : ItineraryMapEvent()
}
