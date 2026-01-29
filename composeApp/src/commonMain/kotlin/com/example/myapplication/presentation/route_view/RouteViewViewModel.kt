package com.example.myapplication.presentation.route_view

import com.example.myapplication.domain.repository.RouteRepository
import com.example.myapplication.presentation.mvi.BaseViewModel

/**
 * RouteView 畫面的 ViewModel
 * 
 * 負責處理路線檢視相關的業務邏輯
 */
class RouteViewViewModel(
    private val routeRepository: RouteRepository
) : BaseViewModel<RouteViewState, RouteViewIntent, RouteViewEvent>(
    initialState = RouteViewState()
) {
    
    override suspend fun processIntent(intent: RouteViewIntent) {
        when (intent) {
            is RouteViewIntent.LoadRoute -> loadRoute(intent.routeId)
            is RouteViewIntent.ExportRoute -> exportRoute(intent.routeId)
        }
    }
    
    /**
     * 載入路線資料
     */
    private suspend fun loadRoute(routeId: String) {
        updateState { copy(isLoading = true, error = null) }
        
        routeRepository.getRoute(routeId)
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
                // 只在需要臨時提示時發送 Event，State 中已有錯誤訊息供 UI 顯示
            }
    }
    
    /**
     * 匯出路線為 JSON
     */
    private suspend fun exportRoute(routeId: String) {
        updateState { copy(isExporting = true) }
        
        routeRepository.exportRoute(routeId)
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
