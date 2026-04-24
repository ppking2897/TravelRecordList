package com.example.myapplication.feature.footprint.presentation

import com.example.myapplication.core.presentation.mvi.UiEvent
import com.example.myapplication.core.presentation.mvi.UiIntent
import com.example.myapplication.core.presentation.mvi.UiState
import com.example.myapplication.feature.footprint.domain.model.TripFootprintGroup

data class FootprintState(
    val isLoading: Boolean = false,
    val groups: List<TripFootprintGroup> = emptyList(),
    val expandedTripId: String? = null,
    val error: String? = null
) : UiState {
    val totalPlaces: Int get() = groups.sumOf { it.placeCount }
    val totalCompleted: Int get() = groups.sumOf { it.completedCount }
    val totalTrips: Int get() = groups.size
}

sealed class FootprintIntent : UiIntent {
    data object Load : FootprintIntent()
    data object Refresh : FootprintIntent()
    data class ToggleExpanded(val tripId: String) : FootprintIntent()
}

sealed class FootprintEvent : UiEvent {
    data class ShowMessage(val message: String) : FootprintEvent()
}
