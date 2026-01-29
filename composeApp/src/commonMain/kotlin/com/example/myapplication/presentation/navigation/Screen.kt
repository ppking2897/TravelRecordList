package com.example.myapplication.presentation.navigation

/**
 * 應用程式的畫面路由定義
 */
sealed class Screen(val route: String) {
    object ItineraryList : Screen("itinerary_list")
    object ItineraryDetail : Screen("itinerary_detail/{itineraryId}") {
        fun createRoute(itineraryId: String) = "itinerary_detail/$itineraryId"
    }
    object AddItinerary : Screen("add_itinerary")
    object EditItinerary : Screen("edit_itinerary/{itineraryId}") {
        fun createRoute(itineraryId: String) = "edit_itinerary/$itineraryId"
    }
    object AddItem : Screen("add_item/{itineraryId}") {
        fun createRoute(itineraryId: String) = "add_item/$itineraryId"
    }
    object EditItem : Screen("edit_item/{itemId}") {
        fun createRoute(itemId: String) = "edit_item/$itemId"
    }
    object TravelHistory : Screen("travel_history")
    object RouteView : Screen("route_view/{routeId}") {
        fun createRoute(routeId: String) = "route_view/$routeId"
    }
}
