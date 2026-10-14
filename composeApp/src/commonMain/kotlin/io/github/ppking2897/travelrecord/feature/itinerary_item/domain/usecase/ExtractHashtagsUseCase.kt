package io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase

import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Hashtag
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.HashtagRepository

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
