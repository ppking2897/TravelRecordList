package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Hashtag
import com.example.myapplication.data.model.ItineraryItem

/**
 * HashtagRepository 的實作
 */
@OptIn(kotlin.time.ExperimentalTime::class)
class HashtagRepositoryImpl(
    private val itineraryRepository: ItineraryRepository
) : HashtagRepository {
    
    companion object {
        // 標籤正則表達式：匹配 # 開頭，後接字母、數字、底線或中文字元
        private val HASHTAG_REGEX = Regex("#([\\w\\u4e00-\\u9fa5]+)")
    }
    
    override fun extractHashtags(text: String): List<Hashtag> {
        val matches = HASHTAG_REGEX.findAll(text)
        val now = kotlin.time.Clock.System.now()
        return matches.map { match ->
            Hashtag(
                tag = match.groupValues[1],
                usageCount = 1,
                firstUsed = now,
                lastUsed = now
            )
        }.distinctBy { it.tag }.toList()
    }
    
    override suspend fun getAllHashtags(): Result<List<Hashtag>> {
        return try {
            val allItineraries = itineraryRepository.getAllItineraries().getOrThrow()
            
            // 收集所有標籤及其使用次數
            val hashtagCounts = mutableMapOf<String, Int>()
            
            val now = kotlin.time.Clock.System.now()
            
            allItineraries.forEach { itinerary ->
                itinerary.items.forEach { item ->
                    item.hashtags.forEach { hashtag ->
                        hashtagCounts[hashtag.tag] = (hashtagCounts[hashtag.tag] ?: 0) + 1
                    }
                }
            }
            
            // 轉換為 Hashtag 列表並按使用次數降序排序
            val hashtags = hashtagCounts.map { (tag, count) ->
                Hashtag(
                    tag = tag,
                    usageCount = count,
                    firstUsed = now,
                    lastUsed = now
                )
            }.sortedByDescending { it.usageCount }
            
            Result.success(hashtags)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun filterItemsByHashtag(items: List<ItineraryItem>, hashtag: String): List<ItineraryItem> {
        return items.filter { item ->
            item.hashtags.any { it.tag.equals(hashtag, ignoreCase = true) }
        }
    }
    
    override suspend fun getHashtagSuggestions(prefix: String): Result<List<Hashtag>> {
        return try {
            if (prefix.isBlank()) {
                return Result.success(emptyList())
            }
            
            val allHashtags = getAllHashtags().getOrThrow()
            val prefixLower = prefix.lowercase()
            
            // 篩選符合前綴的標籤
            val suggestions = allHashtags.filter { hashtag ->
                hashtag.tag.lowercase().startsWith(prefixLower)
            }
            
            Result.success(suggestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
