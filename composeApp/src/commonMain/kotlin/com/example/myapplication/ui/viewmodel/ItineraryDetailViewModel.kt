package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.model.Itinerary
import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.data.repository.ItineraryItemRepository
import com.example.myapplication.data.repository.ItineraryRepository
import com.example.myapplication.domain.usecase.AddItineraryItemUseCase
import com.example.myapplication.domain.usecase.DeleteItineraryItemUseCase
import com.example.myapplication.domain.usecase.FilterItemsByDateUseCase
import com.example.myapplication.domain.usecase.GroupItemsByDateUseCase
import com.example.myapplication.domain.usecase.ItemsByDate
import com.example.myapplication.domain.usecase.UpdateItineraryItemUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime

/**
 * 行程詳情 ViewModel
 * 管理單一行程的詳細資訊和項目
 * 
 * 需求: 2.3, 3.3, 6.1
 */
@ExperimentalTime
class ItineraryDetailViewModel(
    private val itineraryRepository: ItineraryRepository,
    private val itemRepository: ItineraryItemRepository,
    private val addItemUseCase: AddItineraryItemUseCase,
    private val updateItemUseCase: UpdateItineraryItemUseCase,
    private val deleteItemUseCase: DeleteItineraryItemUseCase,
    private val groupItemsByDateUseCase: GroupItemsByDateUseCase,
    private val filterItemsByDateUseCase: FilterItemsByDateUseCase
) : ViewModel() {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)
    
    private val _itinerary = MutableStateFlow<Itinerary?>(null)
    val itinerary: StateFlow<Itinerary?> = _itinerary.asStateFlow()
    
    private val _items = MutableStateFlow<List<ItineraryItem>>(emptyList())
    val items: StateFlow<List<ItineraryItem>> = _items.asStateFlow()
    
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()
    
    private val _dateRange = MutableStateFlow<ClosedRange<LocalDate>?>(null)
    val dateRange: StateFlow<ClosedRange<LocalDate>?> = _dateRange.asStateFlow()
    
    private val _groupedItems = MutableStateFlow<List<ItemsByDate>>(emptyList())
    val groupedItems: StateFlow<List<ItemsByDate>> = _groupedItems.asStateFlow()
    
    private val _filteredItems = MutableStateFlow<List<ItineraryItem>>(emptyList())
    val filteredItems: StateFlow<List<ItineraryItem>> = _filteredItems.asStateFlow()
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * 載入行程詳情
     */
    fun loadItinerary(itineraryId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            itineraryRepository.getItinerary(itineraryId)
                .onSuccess { itinerary ->
                    _itinerary.value = itinerary
                    
                    // 設定日期範圍
                    if (itinerary != null && itinerary.startDate != null && itinerary.endDate != null) {
                        _dateRange.value = itinerary.startDate..itinerary.endDate
                    }
                    
                    // 載入項目
                    if (itinerary != null) {
                        loadItems(itineraryId)
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load itinerary"
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 載入行程項目
     */
    private fun loadItems(itineraryId: String) {
        viewModelScope.launch {
            itemRepository.getItemsByItinerary(itineraryId)
                .onSuccess { items ->
                    _items.value = items
                    updateGroupedAndFilteredItems()
                    updateProgress(itineraryId)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load items"
                }
        }
    }
    
    /**
     * 選擇日期進行篩選
     * 
     * @param date 選中的日期，null 表示顯示所有項目
     */
    fun selectDate(date: LocalDate?) {
        _selectedDate.value = date
        updateGroupedAndFilteredItems()
    }
    
    /**
     * 更新分組和篩選後的項目
     */
    private fun updateGroupedAndFilteredItems() {
        val currentItems = _items.value
        val currentSelectedDate = _selectedDate.value
        
        // 篩選項目
        val filtered = filterItemsByDateUseCase(currentItems, currentSelectedDate)
        _filteredItems.value = filtered
        
        // 分組項目（只在顯示所有項目時分組）
        if (currentSelectedDate == null) {
            _groupedItems.value = groupItemsByDateUseCase(currentItems)
        } else {
            // 選中特定日期時，也進行分組以保持一致的顯示格式
            _groupedItems.value = groupItemsByDateUseCase(filtered)
        }
    }
    
    /**
     * 更新進度
     */
    private fun updateProgress(itineraryId: String) {
        viewModelScope.launch {
            itemRepository.calculateProgress(itineraryId)
                .onSuccess { progress ->
                    _progress.value = progress
                }
        }
    }
    
    /**
     * 切換項目完成狀態
     */
    fun toggleItemCompletion(itemId: String) {
        viewModelScope.launch {
            val currentTimestamp = kotlin.time.Clock.System.now()
            
            itemRepository.toggleCompletion(itemId, currentTimestamp)
                .onSuccess {
                    // 重新載入項目列表
                    _itinerary.value?.let { itinerary ->
                        loadItems(itinerary.id)
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to toggle completion"
                }
        }
    }
    
    /**
     * 刪除項目
     */
    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            deleteItemUseCase(itemId)
                .onSuccess {
                    // 重新載入項目列表
                    _itinerary.value?.let { itinerary ->
                        loadItems(itinerary.id)
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to delete item"
                }
        }
    }
    
    /**
     * 清除錯誤訊息
     */
    fun clearError() {
        _error.value = null
    }
}
