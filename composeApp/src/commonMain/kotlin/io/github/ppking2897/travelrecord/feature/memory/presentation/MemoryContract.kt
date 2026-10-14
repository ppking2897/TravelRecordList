package io.github.ppking2897.travelrecord.feature.memory.presentation

import io.github.ppking2897.travelrecord.core.presentation.mvi.UiEvent
import io.github.ppking2897.travelrecord.core.presentation.mvi.UiIntent
import io.github.ppking2897.travelrecord.core.presentation.mvi.UiState
import io.github.ppking2897.travelrecord.feature.memory.domain.model.TripMemory

data class MemoryState(
    val isLoading: Boolean = false,
    val memories: List<TripMemory> = emptyList(),
    val expandedTripId: String? = null,
    val error: String? = null
) : UiState

sealed class MemoryIntent : UiIntent {
    data object Load : MemoryIntent()
    data object Refresh : MemoryIntent()
    data class ToggleExpanded(val tripId: String) : MemoryIntent()
}

sealed class MemoryEvent : UiEvent {
    data class ShowMessage(val message: String) : MemoryEvent()
}
