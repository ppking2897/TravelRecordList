package com.example.myapplication.feature.itinerary_list.presentation

import com.example.myapplication.feature.itinerary.domain.usecase.DeleteItineraryUseCase
import com.example.myapplication.feature.itinerary.domain.usecase.GetAllItinerariesUseCase
import com.example.myapplication.feature.itinerary.domain.usecase.InitializeSampleDataUseCase
import com.example.myapplication.feature.itinerary.domain.usecase.SearchItinerariesUseCase
import com.example.myapplication.core.presentation.mvi.BaseViewModel

/**
 * ItineraryList 畫面的 ViewModel（MVI 架構）
 */
class ItineraryListViewModel(
    private val getAllItinerariesUseCase: GetAllItinerariesUseCase,
    private val searchUseCase: SearchItinerariesUseCase,
    private val initializeSampleDataUseCase: InitializeSampleDataUseCase,
    private val deleteItineraryUseCase: DeleteItineraryUseCase,
) : BaseViewModel<ItineraryListState, ItineraryListIntent, ItineraryListEvent>(
    initialState = ItineraryListState()
) {

    init {
        handleIntent(ItineraryListIntent.InitializeAndLoad)
    }

    override suspend fun processIntent(intent: ItineraryListIntent) {
        when (intent) {
            is ItineraryListIntent.InitializeAndLoad -> initializeAndLoad()
            is ItineraryListIntent.LoadItineraries -> loadItineraries()
            is ItineraryListIntent.Refresh -> refresh()
            is ItineraryListIntent.Search -> search(intent.query)
            is ItineraryListIntent.DeleteItinerary -> deleteItinerary(intent.id)
        }
    }

    /**
     * 初始化預設資料並載入行程
     */
    private suspend fun initializeAndLoad() {
        updateState { copy(isLoading = true, error = null) }

        // 先嘗試初始化預設資料（如果沒有資料的話）
        initializeSampleDataUseCase()

        // 再載入行程
        loadItineraries()
    }

    /**
     * 載入所有行程
     * 有資料時背景更新，無資料時才顯示 loading
     */
    private suspend fun loadItineraries() {
        val hasData = currentState.itineraries.isNotEmpty()
        if (!hasData) {
            updateState { copy(isLoading = true, error = null) }
        }

        getAllItinerariesUseCase()
            .onSuccess { itineraries ->
                updateState {
                    copy(
                        itineraries = itineraries,
                        isLoading = false
                    )
                }
            }
            .onFailure { exception ->
                if (!hasData) {
                    updateState {
                        copy(
                            isLoading = false,
                            error = exception.message ?: "載入失敗"
                        )
                    }
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
        deleteItineraryUseCase(id)
            .onSuccess {
                handleIntent(ItineraryListIntent.Refresh)
            }
            .onFailure { exception ->
                sendEvent(ItineraryListEvent.ShowError(exception.message ?: "刪除失敗"))
            }
    }

    /**
     * 下拉重新整理（顯示舊資料 + 刷新指示器）
     */
    private suspend fun refresh() {
        updateState { copy(isRefreshing = true, error = null) }

        getAllItinerariesUseCase()
            .onSuccess { itineraries ->
                updateState {
                    copy(itineraries = itineraries, isRefreshing = false)
                }
            }
            .onFailure { exception ->
                updateState {
                    copy(isRefreshing = false, error = exception.message ?: "載入失敗")
                }
            }
    }
}
