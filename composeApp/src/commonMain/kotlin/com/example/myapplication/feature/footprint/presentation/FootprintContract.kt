package com.example.myapplication.feature.footprint.presentation

import com.example.myapplication.core.presentation.mvi.UiEvent
import com.example.myapplication.core.presentation.mvi.UiIntent
import com.example.myapplication.core.presentation.mvi.UiState
import com.example.myapplication.feature.footprint.domain.model.VisitedPlace
import com.example.myapplication.feature.route.domain.model.MapMarker

data class FootprintState(
    val isLoading: Boolean = false,
    val places: List<VisitedPlace> = emptyList(),
    val markers: List<MapMarker> = emptyList(),
    val selectedMarker: MapMarker? = null,
    val error: String? = null
) : UiState {
    val totalPlaces: Int get() = places.size
    val completedPlaces: Int get() = places.count { it.isCompleted }
    val totalTrips: Int get() = places.map { it.tripId }.distinct().size
}

sealed class FootprintIntent : UiIntent {
    data object Load : FootprintIntent()
    data object Refresh : FootprintIntent()
    data class SelectMarker(val marker: MapMarker?) : FootprintIntent()
}

sealed class FootprintEvent : UiEvent {
    data class ShowMessage(val message: String) : FootprintEvent()
}
