@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.feature.footprint.presentation

import androidx.lifecycle.viewModelScope
import io.github.ppking2897.travelrecord.core.presentation.mvi.BaseViewModel
import io.github.ppking2897.travelrecord.feature.footprint.domain.model.TripFootprintGroup
import io.github.ppking2897.travelrecord.feature.footprint.domain.model.VisitedPlace
import io.github.ppking2897.travelrecord.feature.footprint.domain.usecase.GetVisitedPlacesUseCase
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class FootprintViewModel(
    private val getVisitedPlacesUseCase: GetVisitedPlacesUseCase,
    private val clock: Clock,
    private val timeZone: TimeZone,
) :BaseViewModel<FootprintIntent, FootprintState, FootprintEvent>(
    initialState = FootprintState()
) {
    override fun processIntent(intent: FootprintIntent) {
        viewModelScope.launch {
            when (intent) {
                FootprintIntent.Load -> load(showLoadingWhenEmpty = true)
                FootprintIntent.Refresh -> load(showLoadingWhenEmpty = false)
                is FootprintIntent.ToggleExpanded -> toggleExpanded(intent.tripId)
            }
        }
    }

    private suspend fun load(showLoadingWhenEmpty: Boolean) {
        val hasData = state.value.groups.isNotEmpty()
        if (showLoadingWhenEmpty && !hasData) {
            updateState { copy(isLoading = true, error = null) }
        }

        val result = getVisitedPlacesUseCase(clock.todayIn(timeZone))
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
