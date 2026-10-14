package io.github.ppking2897.travelrecord.feature.footprint.presentation

import io.github.ppking2897.travelrecord.feature.footprint.domain.model.TripFootprintGroup

data class FootprintState(
    val isLoading: Boolean = false,
    val groups: List<TripFootprintGroup> = emptyList(),
    val expandedTripId: String? = null,
    val error: String? = null
) {
    val totalPlaces: Int get() = groups.sumOf { it.placeCount }
    val totalCompleted: Int get() = groups.sumOf { it.completedCount }
    val totalTrips: Int get() = groups.size
}

sealed class FootprintIntent {
    data object Load : FootprintIntent()
    data object Refresh : FootprintIntent()
    data class ToggleExpanded(val tripId: String) : FootprintIntent()
}

sealed class FootprintEvent {
    data class ShowMessage(val message: String) : FootprintEvent()
}
