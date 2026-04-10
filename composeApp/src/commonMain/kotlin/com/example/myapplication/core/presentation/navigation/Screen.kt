package com.example.myapplication.core.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 應用程式的 type-safe 路由定義（Navigation 3）
 */
@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object ItineraryList : AppRoute

    @Serializable
    data class ItineraryDetail(val itineraryId: String) : AppRoute

    @Serializable
    data object AddItinerary : AppRoute

    @Serializable
    data class EditItinerary(val itineraryId: String) : AppRoute

    @Serializable
    data class AddItem(val itineraryId: String) : AppRoute

    @Serializable
    data class QuickAddItem(val itineraryId: String, val defaultDate: String) : AppRoute

    @Serializable
    data class EditItem(val itemId: String) : AppRoute

    @Serializable
    data object TravelHistory : AppRoute

    @Serializable
    data class RouteView(val routeId: String) : AppRoute

    @Serializable
    data class ItineraryMap(val itineraryId: String) : AppRoute
}
