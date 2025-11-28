package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.data.model.Location
import com.example.myapplication.data.repository.ItineraryItemRepository
import com.example.myapplication.domain.usecase.GetTravelHistoryUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime

/**
 * 旅遊歷史 ViewModel
 * 管理按地點分組的旅遊歷史
 */
@ExperimentalTime
class TravelHistoryViewModel(
    private val getTravelHistoryUseCase: GetTravelHistoryUseCase,
    private val itemRepository: ItineraryItemRepository
) : ViewModel() {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _historyByLocation =
        MutableStateFlow<Map<Location, List<ItineraryItem>>>(emptyMap())
    val historyByLocation: StateFlow<Map<Location, List<ItineraryItem>>> =
        _historyByLocation.asStateFlow()

    private val _dateFilter = MutableStateFlow<DateRange?>(null)
    val dateFilter: StateFlow<DateRange?> = _dateFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadHistory()
    }

    /**
     * 載入旅遊歷史
     */
    fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            getTravelHistoryUseCase()
                .onSuccess { history ->
                    applyDateFilter(history)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load travel history"
                }

            _isLoading.value = false
        }
    }

    /**
     * 套用日期範圍過濾
     */
    fun filterByDateRange(start: LocalDate, end: LocalDate) {
        _dateFilter.value = DateRange(start, end)

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            itemRepository.getItemsByDateRange(start, end)
                .onSuccess { items ->
                    // 過濾已完成的項目並按地點分組
                    val completedItems = items.filter { it.isCompleted }
                    val grouped = completedItems.groupBy { it.location }

                    // 對每個地點的項目按日期排序
                    val sorted = grouped.mapValues { (_, items) ->
                        items.sortedWith(
                            compareBy<ItineraryItem> { it.date }
                                .thenBy { it.primaryTime() }
                        )
                    }

                    // 按地點名稱排序
                    val sortedMap = sorted.entries
                        .sortedBy { it.key.name }
                        .associate { it.key to it.value }
                    _historyByLocation.value = sortedMap
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to filter by date range"
                }

            _isLoading.value = false
        }
    }

    /**
     * 清除日期過濾
     */
    fun clearFilter() {
        _dateFilter.value = null
        loadHistory()
    }

    /**
     * 套用日期過濾到歷史資料
     */
    private fun applyDateFilter(history: Map<Location, List<ItineraryItem>>) {
        val filter = _dateFilter.value

        if (filter == null) {
            _historyByLocation.value = history
        } else {
            // 過濾日期範圍內的項目
            val filtered = history.mapValues { (_, items) ->
                items.filter { item ->
                    item.date >= filter.start && item.date <= filter.end
                }
            }.filterValues { it.isNotEmpty() }

            _historyByLocation.value = filtered
        }
    }

    /**
     * 清除錯誤訊息
     */
    fun clearError() {
        _error.value = null
    }
}

/**
 * 日期範圍資料類別
 */
data class DateRange(
    val start: LocalDate,
    val end: LocalDate
)
