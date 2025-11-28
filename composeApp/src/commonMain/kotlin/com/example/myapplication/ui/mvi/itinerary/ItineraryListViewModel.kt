package com.example.myapplication.ui.mvi.itinerary

import com.example.myapplication.data.repository.ItineraryRepository
import com.example.myapplication.domain.usecase.SearchItinerariesUseCase
import com.example.myapplication.ui.mvi.BaseViewModel

/**
 * ItineraryList 畫面的 ViewModel（MVI 架構）
 */
class ItineraryListViewModel(
    private val itineraryRepository: ItineraryRepository,
    private val searchUseCase: SearchItinerariesUseCase
) : BaseViewModel<ItineraryListState, ItineraryListIntent, ItineraryListEvent>(
    initialState = ItineraryListState()
) {
    
    init {
        handleIntent(ItineraryListIntent.LoadItineraries)
    }
    
    override suspend fun processIntent(intent: ItineraryListIntent) {
        when (intent) {
            is ItineraryListIntent.LoadItineraries -> loadItineraries()
            is ItineraryListIntent.Refresh -> refresh()
            is ItineraryListIntent.Search -> search(intent.query)
            is ItineraryListIntent.DeleteItinerary -> deleteItinerary(intent.id)
        }
    }
    
    /**
     * 載入所有行程
     */
    private suspend fun loadItineraries() {
        updateState { copy(isLoading = true, error = null) }
        
        itineraryRepository.getAllItineraries()
            .onSuccess { itineraries ->
                updateState { 
                    copy(
                        itineraries = itineraries,
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
     * 搜尋行程
     */
    private suspend fun search(query: String) {
        updateState { copy(searchQuery = query, isLoading = true, error = null) }
        
        if (query.isBlank()) {
            loadItineraries()
        } else {
            searchUseCase(query)
                .onSuccess { results ->
                    updateState { 
                        copy(
                            itineraries = results,
                            isLoading = false
                        )
                    }
                }
                .onFailure { exception ->
                    val errorMessage = exception.message ?: "搜尋失敗"
                    updateState { 
                        copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                }
        }
    }
    
    /**
     * 刪除行程
     */
    private suspend fun deleteItinerary(id: String) {
        itineraryRepository.deleteItinerary(id)
            .onSuccess {
                handleIntent(ItineraryListIntent.Refresh)
            }
            .onFailure { exception ->
                sendEvent(ItineraryListEvent.ShowError(exception.message ?: "刪除失敗"))
            }
    }
    
    /**
     * 重新整理
     */
    private suspend fun refresh() {
        loadItineraries()
    }
}
