@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.feature.footprint.presentation

import com.example.myapplication.core.presentation.mvi.BaseViewModel
import com.example.myapplication.feature.footprint.domain.model.VisitedPlace
import com.example.myapplication.feature.footprint.domain.usecase.GetVisitedPlacesUseCase
import com.example.myapplication.feature.route.domain.model.MapMarker

class FootprintViewModel(
    private val getVisitedPlacesUseCase: GetVisitedPlacesUseCase
) : BaseViewModel<FootprintState, FootprintIntent, FootprintEvent>(
    initialState = FootprintState()
) {
    override suspend fun processIntent(intent: FootprintIntent) {
        when (intent) {
            FootprintIntent.Load -> load(showLoadingWhenEmpty = true)
            FootprintIntent.Refresh -> load(showLoadingWhenEmpty = false)
            is FootprintIntent.SelectMarker -> updateState { copy(selectedMarker = intent.marker) }
        }
    }

    private suspend fun load(showLoadingWhenEmpty: Boolean) {
        val hasData = currentState.places.isNotEmpty()
        if (showLoadingWhenEmpty && !hasData) {
            updateState { copy(isLoading = true, error = null) }
        }

        val result = getVisitedPlacesUseCase()
        val places = result.getOrNull()
        if (places != null) {
            val markers = places.toMarkers()
            updateState {
                copy(
                    places = places,
                    markers = markers,
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

    private fun List<VisitedPlace>.toMarkers(): List<MapMarker> {
        return mapIndexedNotNull { index, place ->
            val lat = place.latitude ?: return@mapIndexedNotNull null
            val lng = place.longitude ?: return@mapIndexedNotNull null
            MapMarker(
                id = place.itemId,
                latitude = lat,
                longitude = lng,
                title = place.activity.ifBlank { place.locationName },
                locationName = place.locationName,
                date = place.date,
                order = index + 1,
                isCompleted = place.isCompleted
            )
        }
    }
}
