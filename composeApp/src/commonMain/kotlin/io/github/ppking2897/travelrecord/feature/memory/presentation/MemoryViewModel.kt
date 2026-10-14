@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.feature.memory.presentation

import io.github.ppking2897.travelrecord.core.presentation.mvi.BaseViewModel
import io.github.ppking2897.travelrecord.feature.memory.domain.usecase.GetMemoriesUseCase

class MemoryViewModel(
    private val getMemoriesUseCase: GetMemoriesUseCase
) : BaseViewModel<MemoryState, MemoryIntent, MemoryEvent>(
    initialState = MemoryState()
) {
    override suspend fun processIntent(intent: MemoryIntent) {
        when (intent) {
            MemoryIntent.Load -> load(showLoadingWhenEmpty = true)
            MemoryIntent.Refresh -> load(showLoadingWhenEmpty = false)
            is MemoryIntent.ToggleExpanded -> toggleExpanded(intent.tripId)
        }
    }

    private suspend fun load(showLoadingWhenEmpty: Boolean) {
        val hasData = currentState.memories.isNotEmpty()
        if (showLoadingWhenEmpty && !hasData) {
            updateState { copy(isLoading = true, error = null) }
        }

        val result = getMemoriesUseCase()
        val memories = result.getOrNull()
        if (memories != null) {
            updateState { copy(memories = memories, isLoading = false, error = null) }
        } else {
            val message = result.exceptionOrNull()?.message ?: "載入回憶失敗"
            updateState { copy(isLoading = false, error = message) }
            sendEvent(MemoryEvent.ShowMessage(message))
        }
    }

    private fun toggleExpanded(tripId: String) {
        updateState {
            copy(expandedTripId = if (expandedTripId == tripId) null else tripId)
        }
    }
}
