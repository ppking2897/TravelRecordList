package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.entity.Hashtag
import com.example.myapplication.domain.repository.HashtagRepository

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
