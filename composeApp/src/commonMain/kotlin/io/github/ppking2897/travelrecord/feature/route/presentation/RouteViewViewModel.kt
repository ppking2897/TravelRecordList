package io.github.ppking2897.travelrecord.feature.route.presentation

import androidx.lifecycle.viewModelScope
import io.github.ppking2897.travelrecord.feature.route.domain.usecase.ExportRouteUseCase
import io.github.ppking2897.travelrecord.feature.route.domain.usecase.GetRouteUseCase
import io.github.ppking2897.travelrecord.core.presentation.mvi.BaseViewModel
import kotlinx.coroutines.launch

/**
 * RouteView 畫面的 ViewModel
 *
 * 負責處理路線檢視相關的業務邏輯
 */
class RouteViewViewModel(
    private val getRouteUseCase: GetRouteUseCase,
    private val exportRouteUseCase: ExportRouteUseCase
) : BaseViewModel<RouteViewIntent, RouteViewState, RouteViewEvent>(
    initialState = RouteViewState()
) {

    override fun processIntent(intent: RouteViewIntent) {
        viewModelScope.launch {
            when (intent) {
                is RouteViewIntent.LoadRoute -> loadRoute(intent.routeId)
                is RouteViewIntent.ExportRoute -> exportRoute(intent.routeId)
            }
        }
    }

    /**
     * 載入路線資料
     */
    private suspend fun loadRoute(routeId: String) {
        updateState { copy(isLoading = true, error = null) }

        getRouteUseCase(routeId)
            .onSuccess { route ->
                if (route != null) {
                    updateState {
                        copy(
                            route = route,
                            isLoading = false
                        )
                    }
                } else {
                    updateState {
                        copy(
                            isLoading = false,
                            error = "找不到路線"
                        )
                    }
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
     * 匯出路線為 JSON
     */
    private suspend fun exportRoute(routeId: String) {
        updateState { copy(isExporting = true) }

        exportRouteUseCase(routeId)
            .onSuccess { json ->
                updateState { copy(isExporting = false) }
                sendEvent(RouteViewEvent.ExportSuccess(json))
            }
            .onFailure { exception ->
                updateState { copy(isExporting = false) }
                sendEvent(RouteViewEvent.ShowError(exception.message ?: "匯出失敗"))
            }
    }
}
