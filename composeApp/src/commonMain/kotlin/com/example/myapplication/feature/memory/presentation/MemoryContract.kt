package com.example.myapplication.feature.memory.presentation

import com.example.myapplication.core.presentation.mvi.UiEvent
import com.example.myapplication.core.presentation.mvi.UiIntent
import com.example.myapplication.core.presentation.mvi.UiState
import com.example.myapplication.feature.memory.domain.model.TripMemory

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
