package com.example.myapplication.feature.itinerary_item.domain.usecase

import com.example.myapplication.feature.itinerary_item.domain.model.ItineraryItem
import com.example.myapplication.feature.itinerary_item.domain.repository.HashtagRepository

/**
 * 根據標籤篩選 Use Case
 */
class FilterByHashtagUseCase(
    private val hashtagRepository: HashtagRepository
) {
    operator fun invoke(items: List<ItineraryItem>, hashtag: String): List<ItineraryItem> {
        return hashtagRepository.filterItemsByHashtag(items, hashtag)
    }
}
