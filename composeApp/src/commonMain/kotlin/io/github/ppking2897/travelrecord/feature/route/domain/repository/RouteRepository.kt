package io.github.ppking2897.travelrecord.feature.route.domain.repository

import io.github.ppking2897.travelrecord.feature.route.domain.model.Route

/**
 * 路線資料存取介面
 */
interface RouteRepository {
    /**
     * 建立新路線
     */
    suspend fun createRoute(route: Route): Result<Route>

    /**
     * 取得指定 ID 的路線
     */
    suspend fun getRoute(id: String): Result<Route?>

    /**
     * 匯出路線為 JSON 格式
     */
    suspend fun exportRoute(id: String): Result<String>

    /**
     * 從行程建立路線
     */
    suspend fun createRouteFromItinerary(itineraryId: String, routeId: String): Result<Route>
}
