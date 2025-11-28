package com.example.myapplication.domain.usecase

import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.data.repository.HashtagRepository

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
