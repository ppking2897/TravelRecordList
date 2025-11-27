package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Route

/**
 * 路線資料存取介面
 */
interface RouteRepository {
    /**
     * 建立新路線
     * 
     * @param route 路線資料
     * @return 建立成功的路線或錯誤
     */
    suspend fun createRoute(route: Route): Result<Route>
    
    /**
     * 取得指定 ID 的路線
     * 
     * @param id 路線 ID
     * @return 路線資料或 null（如果不存在）
     */
    suspend fun getRoute(id: String): Result<Route?>
    
    /**
     * 匯出路線為 JSON 格式
     * 
     * @param id 路線 ID
     * @return JSON 字串或錯誤
     */
    suspend fun exportRoute(id: String): Result<String>
}
