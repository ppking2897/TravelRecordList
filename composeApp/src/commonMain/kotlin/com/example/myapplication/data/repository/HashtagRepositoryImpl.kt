@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.data.repository

import com.example.myapplication.domain.entity.Hashtag
import com.example.myapplication.domain.entity.ItineraryItem
import com.example.myapplication.domain.repository.HashtagRepository
import com.example.myapplication.domain.repository.ItineraryRepository
import kotlin.time.Clock

/**
 * HashtagRepository 的實作
 */
class HashtagRepositoryImpl(
    private val itineraryRepository: ItineraryRepository
) : HashtagRepository {

    companion object {
        private val HASHTAG_REGEX = Regex("""#([\w\u4e00-\u9fa5]+)""")
    }

    override fun extractHashtags(text: String): List<Hashtag> {
        val matches = HASHTAG_REGEX.findAll(text)
        val now = Clock.System.now()
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

            val hashtagCounts = mutableMapOf<String, Int>()

            val now = Clock.System.now()

            allItineraries.forEach { itinerary ->
                itinerary.items.forEach { item ->
                    item.hashtags.forEach { hashtag ->
                        hashtagCounts[hashtag.tag] = (hashtagCounts[hashtag.tag] ?: 0) + 1
                    }
                }
            }

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

            val suggestions = allHashtags.filter { hashtag ->
                hashtag.tag.lowercase().startsWith(prefixLower)
            }

            Result.success(suggestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
