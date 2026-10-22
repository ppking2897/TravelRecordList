package io.github.ppking2897.travelrecord.feature.route.domain.repository

import io.github.ppking2897.travelrecord.feature.route.domain.model.Route

/**
 * 路線資料存取介面
 *
 * 路線不存檔：每次都依行程當下的項目排出來，行程改了，下次打開路線頁就跟著變。
 */
interface RouteRepository {
    /**
     * 依行程目前的項目排出路線
     *
     * 項目依日期、時間排序，同名地點只留第一次；不到兩個不同地點時回傳失敗。
     */
    suspend fun getRouteForItinerary(itineraryId: String): Result<Route>

    /**
     * 匯出行程的路線為 JSON 格式
     */
    suspend fun exportRoute(itineraryId: String): Result<String>

    /**
     * 刪除舊版存下的路線：舊版每按一次「生成路線」就另存一份，App 裡沒有地方再讀到
     */
    suspend fun deleteStoredRoutes(): Result<Unit>
}
