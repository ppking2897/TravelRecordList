package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Hashtag
import com.example.myapplication.data.model.ItineraryItem

/**
 * Hashtag Repository 介面
 */
interface HashtagRepository {
    /**
     * 從文字中提取標籤
     * @param text 包含標籤的文字（例如備註）
     * @return 提取出的標籤列表
     */
    fun extractHashtags(text: String): List<Hashtag>
    
    /**
     * 取得所有標籤及其使用次數
     * @return 標籤列表，按使用次數降序排序
     */
    suspend fun getAllHashtags(): Result<List<Hashtag>>
    
    /**
     * 根據標籤篩選行程項目
     * @param items 要篩選的項目列表
     * @param hashtag 標籤名稱
     * @return 包含該標籤的項目列表
     */
    fun filterItemsByHashtag(items: List<ItineraryItem>, hashtag: String): List<ItineraryItem>
    
    /**
     * 取得標籤建議（自動完成）
     * @param prefix 標籤前綴
     * @return 符合前綴的標籤列表
     */
    suspend fun getHashtagSuggestions(prefix: String): Result<List<Hashtag>>
}
