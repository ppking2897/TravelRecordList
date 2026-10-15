package io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository

import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Hashtag
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem

/**
 * Hashtag Repository 介面
 */
interface HashtagRepository {
    /**
     * 從文字中提取標籤
     */
    fun extractHashtags(text: String): List<Hashtag>

    /**
     * 根據標籤篩選項目
     */
    fun filterItemsByHashtag(items: List<ItineraryItem>, hashtag: String): List<ItineraryItem>
}
