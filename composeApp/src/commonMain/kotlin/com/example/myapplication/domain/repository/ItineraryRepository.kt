package com.example.myapplication.domain.repository

import com.example.myapplication.domain.entity.Itinerary

/**
 * 行程資料存取介面
 */
interface ItineraryRepository {
    /**
     * 建立新行程
     */
    suspend fun createItinerary(itinerary: Itinerary): Result<Itinerary>

    /**
     * 取得指定 ID 的行程
     */
    suspend fun getItinerary(id: String): Result<Itinerary?>

    /**
     * 取得所有行程
     */
    suspend fun getAllItineraries(): Result<List<Itinerary>>

    /**
     * 更新行程
     */
    suspend fun updateItinerary(itinerary: Itinerary): Result<Itinerary>

    /**
     * 刪除行程
     */
    suspend fun deleteItinerary(id: String): Result<Unit>

    /**
     * 搜尋行程
     */
    suspend fun searchItineraries(query: String): Result<List<Itinerary>>
}
