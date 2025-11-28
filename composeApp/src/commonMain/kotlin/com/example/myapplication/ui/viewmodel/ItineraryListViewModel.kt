package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.model.Itinerary
import com.example.myapplication.data.repository.ItineraryRepository
import com.example.myapplication.domain.usecase.SearchItinerariesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 行程列表 ViewModel
 * 管理行程列表的 UI 狀態和操作
 */
class ItineraryListViewModel(
    private val itineraryRepository: ItineraryRepository,
    private val searchUseCase: SearchItinerariesUseCase
): ViewModel() {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)
    
    private val _itineraries = MutableStateFlow<List<Itinerary>>(emptyList())
    val itineraries: StateFlow<List<Itinerary>> = _itineraries.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadItineraries()
    }
    
    /**
     * 載入所有行程
     */
    fun loadItineraries() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            itineraryRepository.getAllItineraries()
                .onSuccess { itineraries ->
                    _itineraries.value = itineraries
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load itineraries"
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 搜尋行程
     */
    fun search(query: String) {
        _searchQuery.value = query
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            if (query.isBlank()) {
                // 如果搜尋查詢為空，顯示所有行程
                loadItineraries()
            } else {
                searchUseCase(query)
                    .onSuccess { results ->
                        _itineraries.value = results
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Search failed"
                    }
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 重新整理
     */
    fun refresh() {
        loadItineraries()
    }
    
    /**
     * 刪除行程
     */
    fun deleteItinerary(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            itineraryRepository.deleteItinerary(id)
                .onSuccess {
                    // 重新載入列表
                    loadItineraries()
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "刪除失敗"
                    _isLoading.value = false
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
