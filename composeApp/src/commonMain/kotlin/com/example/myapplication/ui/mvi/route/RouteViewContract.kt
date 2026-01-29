package com.example.myapplication.ui.mvi.route

import com.example.myapplication.domain.entity.Route
import com.example.myapplication.ui.mvi.UiEvent
import com.example.myapplication.ui.mvi.UiIntent
import com.example.myapplication.ui.mvi.UiState

/**
 * RouteView 畫面的 MVI Contract
 */

/**
 * RouteView State
 */
data class RouteViewState(
    val route: Route? = null,
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val error: String? = null
) : UiState

/**
 * RouteView Intent
 */
sealed class RouteViewIntent : UiIntent {
    data class LoadRoute(val routeId: String) : RouteViewIntent()
    data class ExportRoute(val routeId: String) : RouteViewIntent()
}

/**
 * RouteView Event
 */
sealed class RouteViewEvent : UiEvent {
    object NavigateBack : RouteViewEvent()
    data class ExportSuccess(val json: String) : RouteViewEvent()
    data class ShowError(val message: String) : RouteViewEvent()
}
