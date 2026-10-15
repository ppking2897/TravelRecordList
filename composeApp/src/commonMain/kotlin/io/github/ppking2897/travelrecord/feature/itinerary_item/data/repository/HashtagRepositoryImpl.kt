@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository

import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Hashtag
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.HashtagRepository
import kotlin.time.Clock

/**
 * HashtagRepository 的實作
 */
class HashtagRepositoryImpl : HashtagRepository {

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

    override fun filterItemsByHashtag(items: List<ItineraryItem>, hashtag: String): List<ItineraryItem> {
        return items.filter { item ->
            item.hashtags.any { it.tag.equals(hashtag, ignoreCase = true) }
        }
    }
}
