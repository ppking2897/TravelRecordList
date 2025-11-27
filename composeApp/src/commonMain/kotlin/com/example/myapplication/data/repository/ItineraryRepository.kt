package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Itinerary

/**
 * 行程資料存取介面
 */
interface ItineraryRepository {
    /**
     * 建立新行程
     * 
     * @param itinerary 行程資料
     * @return 建立成功的行程或錯誤
     */
    suspend fun createItinerary(itinerary: Itinerary): Result<Itinerary>
    
    /**
     * 取得指定 ID 的行程
     * 
     * @param id 行程 ID
     * @return 行程資料或 null（如果不存在）
     */
    suspend fun getItinerary(id: String): Result<Itinerary?>
    
    /**
     * 取得所有行程
     * 
     * @return 所有行程列表
     */
    suspend fun getAllItineraries(): Result<List<Itinerary>>
    
    /**
     * 更新行程
     * 
     * @param itinerary 更新後的行程資料
     * @return 更新成功的行程或錯誤
     */
    suspend fun updateItinerary(itinerary: Itinerary): Result<Itinerary>
    
    /**
     * 刪除行程
     * 
     * @param id 行程 ID
     * @return 成功或錯誤
     */
    suspend fun deleteItinerary(id: String): Result<Unit>
    
    /**
     * 搜尋行程
     * 
     * @param query 搜尋關鍵字
     * @return 符合條件的行程列表
     */
    suspend fun searchItineraries(query: String): Result<List<Itinerary>>
}
