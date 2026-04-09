package com.example.myapplication.feature.itinerary_item.domain.usecase

import com.example.myapplication.feature.itinerary_item.domain.model.Hashtag
import com.example.myapplication.feature.itinerary_item.domain.repository.HashtagRepository

/**
 * 提取標籤 Use Case
 */
class ExtractHashtagsUseCase(
    private val hashtagRepository: HashtagRepository
) {
    operator fun invoke(text: String): List<Hashtag> {
        return hashtagRepository.extractHashtags(text)
    }
}
