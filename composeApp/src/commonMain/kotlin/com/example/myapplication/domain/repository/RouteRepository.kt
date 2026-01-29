package com.example.myapplication.domain.repository

import com.example.myapplication.domain.entity.Route

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
