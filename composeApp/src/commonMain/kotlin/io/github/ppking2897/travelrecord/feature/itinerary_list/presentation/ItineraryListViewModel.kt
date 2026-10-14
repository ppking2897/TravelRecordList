package io.github.ppking2897.travelrecord.feature.itinerary_list.presentation

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.DeleteItineraryUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetAllItinerariesUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.InitializeSampleDataUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.SearchItinerariesUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.GetItemsByItineraryUseCase
import io.github.ppking2897.travelrecord.core.presentation.mvi.BaseViewModel

/**
 * ItineraryList 畫面的 ViewModel（MVI 架構）
 */
class ItineraryListViewModel(
    private val getAllItinerariesUseCase: GetAllItinerariesUseCase,
    private val searchUseCase: SearchItinerariesUseCase,
    private val getItemsByItineraryUseCase: GetItemsByItineraryUseCase,
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

        val result = getAllItinerariesUseCase()
        val itineraries = result.getOrNull()
        if (itineraries != null) {
            val hydrated = hydrateItems(itineraries)
            updateState { copy(itineraries = hydrated, isLoading = false) }
        } else if (!hasData) {
            val message = result.exceptionOrNull()?.message ?: "載入失敗"
            updateState { copy(isLoading = false, error = message) }
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
            val result = searchUseCase(query)
            val results = result.getOrNull()
            if (results != null) {
                val hydrated = hydrateItems(results)
                updateState { copy(itineraries = hydrated, isLoading = false) }
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "搜尋失敗"
                updateState { copy(isLoading = false, error = errorMessage) }
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

        val result = getAllItinerariesUseCase()
        val itineraries = result.getOrNull()
        if (itineraries != null) {
            val hydrated = hydrateItems(itineraries)
            updateState { copy(itineraries = hydrated, isRefreshing = false) }
        } else {
            val message = result.exceptionOrNull()?.message ?: "載入失敗"
            updateState { copy(isRefreshing = false, error = message) }
        }
    }

    private suspend fun hydrateItems(itineraries: List<Itinerary>): List<Itinerary> {
        return itineraries.map { itinerary ->
            val items = getItemsByItineraryUseCase(itinerary.id).getOrElse { emptyList() }
            itinerary.copy(items = items)
        }
    }
}
