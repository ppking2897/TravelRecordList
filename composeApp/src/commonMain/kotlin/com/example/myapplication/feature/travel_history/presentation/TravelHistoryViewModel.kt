package com.example.myapplication.feature.travel_history.presentation

import kotlinx.datetime.LocalDate
import com.example.myapplication.feature.travel_history.domain.usecase.FilterHistoryByDateRangeUseCase
import com.example.myapplication.feature.travel_history.domain.usecase.GetTravelHistoryUseCase
import com.example.myapplication.core.presentation.mvi.BaseViewModel
import kotlin.time.ExperimentalTime

/**
 * TravelHistory 畫面的 ViewModel（MVI 架構）
 */
@ExperimentalTime
class TravelHistoryViewModel(
    private val getTravelHistoryUseCase: GetTravelHistoryUseCase,
    private val filterHistoryByDateRangeUseCase: FilterHistoryByDateRangeUseCase
) : BaseViewModel<TravelHistoryState, TravelHistoryIntent, TravelHistoryEvent>(
    initialState = TravelHistoryState()
) {

    init {
        handleIntent(TravelHistoryIntent.LoadHistory)
    }

    override suspend fun processIntent(intent: TravelHistoryIntent) {
        when (intent) {
            is TravelHistoryIntent.LoadHistory -> loadHistory()
            is TravelHistoryIntent.FilterByDateRange -> filterByDateRange(intent.dateRange)
            is TravelHistoryIntent.ClearFilter -> clearFilter()
            is TravelHistoryIntent.ToggleItemExpansion -> toggleItemExpansion(intent.itemId)
        }
    }

    /**
     * 載入旅遊歷史
     */
    private suspend fun loadHistory() {
        updateState { copy(isLoading = true, error = null) }

        getTravelHistoryUseCase()
            .onSuccess { history ->
                // 將 Map<Location, List<ItineraryItem>> 轉換為 Map<String, List<ItineraryItem>>
                val historyByLocationName = history.entries.associate { (location, items) ->
                    location.name to items
                }

                // 如果有日期過濾，套用過濾
                val filteredHistory = currentState.dateFilter?.let { dateRange ->
                    historyByLocationName.mapValues { (_, items) ->
                        items.filter { item ->
                            item.date >= dateRange.start && item.date <= dateRange.endInclusive
                        }
                    }.filterValues { it.isNotEmpty() }
                } ?: historyByLocationName

                updateState {
                    copy(
                        historyByLocation = filteredHistory,
                        isLoading = false
                    )
                }
            }
            .onFailure { exception ->
                val errorMessage = exception.message ?: "載入失敗"
                updateState {
                    copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            }
    }

    /**
     * 套用日期範圍過濾
     */
    private suspend fun filterByDateRange(dateRange: ClosedRange<LocalDate>) {
        updateState { copy(dateFilter = dateRange, isLoading = true, error = null) }

        filterHistoryByDateRangeUseCase(dateRange.start, dateRange.endInclusive)
            .onSuccess { sortedMap ->
                updateState {
                    copy(
                        historyByLocation = sortedMap,
                        isLoading = false
                    )
                }
            }
            .onFailure { exception ->
                val errorMessage = exception.message ?: "過濾失敗"
                updateState {
                    copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            }
    }

    /**
     * 清除日期過濾
     */
    private suspend fun clearFilter() {
        updateState { copy(dateFilter = null) }
        loadHistory()
    }

    /**
     * 切換項目展開/收合狀態
     */
    private fun toggleItemExpansion(itemId: String) {
        updateState {
            copy(
                expandedItemIds = if (itemId in expandedItemIds) {
                    expandedItemIds - itemId
                } else {
                    expandedItemIds + itemId
                }
            )
        }
    }
}
