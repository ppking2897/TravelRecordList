@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.feature.footprint.presentation

import io.github.ppking2897.travelrecord.core.presentation.mvi.BaseViewModel
import io.github.ppking2897.travelrecord.feature.footprint.domain.model.TripFootprintGroup
import io.github.ppking2897.travelrecord.feature.footprint.domain.model.VisitedPlace
import io.github.ppking2897.travelrecord.feature.footprint.domain.usecase.GetVisitedPlacesUseCase

class FootprintViewModel(
    private val getVisitedPlacesUseCase: GetVisitedPlacesUseCase
) : BaseViewModel<FootprintState, FootprintIntent, FootprintEvent>(
    initialState = FootprintState()
) {
    override suspend fun processIntent(intent: FootprintIntent) {
        when (intent) {
            FootprintIntent.Load -> load(showLoadingWhenEmpty = true)
            FootprintIntent.Refresh -> load(showLoadingWhenEmpty = false)
            is FootprintIntent.ToggleExpanded -> toggleExpanded(intent.tripId)
        }
    }

    private suspend fun load(showLoadingWhenEmpty: Boolean) {
        val hasData = currentState.groups.isNotEmpty()
        if (showLoadingWhenEmpty && !hasData) {
            updateState { copy(isLoading = true, error = null) }
        }

        val result = getVisitedPlacesUseCase()
        val places = result.getOrNull()
        if (places != null) {
            val groups = places.groupByTrip()
            updateState {
                copy(
                    groups = groups,
                    isLoading = false,
                    error = null
                )
            }
        } else {
            val message = result.exceptionOrNull()?.message ?: "載入足跡失敗"
            updateState { copy(isLoading = false, error = message) }
            sendEvent(FootprintEvent.ShowMessage(message))
        }
    }

    private fun toggleExpanded(tripId: String) {
        updateState {
            copy(expandedTripId = if (expandedTripId == tripId) null else tripId)
        }
    }

    private fun List<VisitedPlace>.groupByTrip(): List<TripFootprintGroup> {
        return groupBy { it.tripId }
            .map { (_, tripPlaces) ->
                val sortedPlaces = tripPlaces.sortedBy { it.date }
                val first = tripPlaces.first()
                TripFootprintGroup(
                    tripId = first.tripId,
                    tripTitle = first.tripTitle,
                    startDate = sortedPlaces.firstOrNull()?.date,
                    endDate = sortedPlaces.lastOrNull()?.date,
                    places = sortedPlaces
                )
            }
            .sortedByDescending { it.endDate }
    }
}
