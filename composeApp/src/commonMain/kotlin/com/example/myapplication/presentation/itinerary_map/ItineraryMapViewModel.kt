package com.example.myapplication.presentation.itinerary_map

import com.example.myapplication.domain.entity.MapMarker
import com.example.myapplication.domain.repository.ItineraryRepository
import com.example.myapplication.domain.usecase.GetMapMarkersUseCase
import com.example.myapplication.presentation.mvi.BaseViewModel
import kotlinx.datetime.LocalDate

/**
 * 地圖畫面的 ViewModel
 */
class ItineraryMapViewModel(
    private val itineraryRepository: ItineraryRepository,
    private val getMapMarkersUseCase: GetMapMarkersUseCase
) : BaseViewModel<ItineraryMapState, ItineraryMapIntent, ItineraryMapEvent>(
    ItineraryMapState()
) {
    private var allMarkers: List<MapMarker> = emptyList()

    override suspend fun processIntent(intent: ItineraryMapIntent) {
        when (intent) {
            is ItineraryMapIntent.LoadMapData -> loadMapData(intent.itineraryId)
            is ItineraryMapIntent.SelectMarker -> selectMarker(intent.marker)
            is ItineraryMapIntent.FilterByDate -> filterByDate(intent.date)
            is ItineraryMapIntent.DismissError -> updateState { copy(error = null) }
        }
    }

    private suspend fun loadMapData(itineraryId: String) {
        updateState { copy(isLoading = true, error = null) }

        try {
            // 載入行程資訊
            val itineraryResult = itineraryRepository.getItinerary(itineraryId)
            val markersResult = getMapMarkersUseCase(itineraryId)

            itineraryResult.onSuccess { itinerary ->
                markersResult.onSuccess { markers ->
                    allMarkers = markers
                    updateState {
                        copy(
                            itinerary = itinerary,
                            markers = markers,
                            isLoading = false,
                            hasNoMarkersToShow = markers.isEmpty()
                        )
                    }
                }.onFailure { e ->
                    updateState {
                        copy(
                            itinerary = itinerary,
                            isLoading = false,
                            error = e.message ?: "載入標記失敗"
                        )
                    }
                }
            }.onFailure { e ->
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "載入行程失敗"
                    )
                }
            }
        } catch (e: Exception) {
            updateState {
                copy(
                    isLoading = false,
                    error = e.message ?: "載入失敗"
                )
            }
        }
    }

    private suspend fun selectMarker(marker: MapMarker?) {
        updateState { copy(selectedMarker = marker) }
        marker?.let {
            sendEvent(ItineraryMapEvent.CenterOnMarker(it))
        }
    }

    private fun filterByDate(date: LocalDate?) {
        val filteredMarkers = if (date == null) {
            allMarkers
        } else {
            allMarkers.filter { it.date == date }
        }

        updateState {
            copy(
                selectedDate = date,
                markers = filteredMarkers,
                selectedMarker = null,
                hasNoMarkersToShow = filteredMarkers.isEmpty() && allMarkers.isNotEmpty()
            )
        }
    }
}
