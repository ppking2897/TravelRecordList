package io.github.ppking2897.travelrecord.feature.route.presentation

import io.github.ppking2897.travelrecord.feature.route.domain.model.Route

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
)

/**
 * RouteView Intent
 */
sealed class RouteViewIntent {
    data class LoadRoute(val routeId: String) : RouteViewIntent()
    data class ExportRoute(val routeId: String) : RouteViewIntent()
}

/**
 * RouteView Event
 */
sealed class RouteViewEvent {
    object NavigateBack : RouteViewEvent()
    data class ExportSuccess(val json: String) : RouteViewEvent()
    data class ShowError(val message: String) : RouteViewEvent()
}
