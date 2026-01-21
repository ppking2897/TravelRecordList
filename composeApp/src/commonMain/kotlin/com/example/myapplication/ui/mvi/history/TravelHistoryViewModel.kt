package com.example.myapplication.ui.mvi.history

import com.example.myapplication.data.repository.ItineraryItemRepository
import com.example.myapplication.domain.usecase.GetTravelHistoryUseCase
import com.example.myapplication.ui.mvi.BaseViewModel
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime

/**
 * TravelHistory 畫面的 ViewModel（MVI 架構）
 */
@ExperimentalTime
class TravelHistoryViewModel(
    private val getTravelHistoryUseCase: GetTravelHistoryUseCase,
    private val itemRepository: ItineraryItemRepository
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
        
        itemRepository.getItemsByDateRange(dateRange.start, dateRange.endInclusive)
            .onSuccess { items ->
                // 過濾已完成的項目並按地點分組
                val completedItems = items.filter { it.isCompleted }
                val grouped = completedItems.groupBy { it.location.name }
                
                // 對每個地點的項目按日期排序
                val sorted = grouped.mapValues { (_, locationItems) ->
                    locationItems.sortedWith(
                        compareBy<com.example.myapplication.data.model.ItineraryItem> { it.date }
                            .thenBy { it.primaryTime() }
                    )
                }
                
                // 按地點名稱排序
                val sortedMap = sorted.entries
                    .sortedBy { it.key }
                    .associate { it.key to it.value }
                
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
}
