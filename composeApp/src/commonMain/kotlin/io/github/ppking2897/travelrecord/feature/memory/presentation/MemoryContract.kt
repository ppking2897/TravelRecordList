package io.github.ppking2897.travelrecord.feature.memory.presentation

import io.github.ppking2897.travelrecord.feature.memory.domain.model.TripMemory

data class MemoryState(
    val isLoading: Boolean = false,
    val memories: List<TripMemory> = emptyList(),
    val expandedTripId: String? = null,
    val error: String? = null
)

sealed class MemoryIntent {
    data object Load : MemoryIntent()
    data object Refresh : MemoryIntent()
    data class ToggleExpanded(val tripId: String) : MemoryIntent()
}

sealed class MemoryEvent {
    data class ShowMessage(val message: String) : MemoryEvent()
}
