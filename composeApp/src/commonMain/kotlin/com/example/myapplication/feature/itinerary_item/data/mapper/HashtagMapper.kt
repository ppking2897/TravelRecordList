package com.example.myapplication.feature.itinerary_item.data.mapper

import com.example.myapplication.feature.itinerary_item.data.model.HashtagResponse
import com.example.myapplication.feature.itinerary_item.domain.model.Hashtag

/**
 * Hashtag Response ↔ Domain 轉換
 */
object HashtagMapper {
    fun toDomain(dto: HashtagResponse): Hashtag = Hashtag(
        tag = dto.tag,
        usageCount = dto.usageCount,
        firstUsed = dto.firstUsed,
        lastUsed = dto.lastUsed
    )

    fun toDto(entity: Hashtag): HashtagResponse = HashtagResponse(
        tag = entity.tag,
        usageCount = entity.usageCount,
        firstUsed = entity.firstUsed,
        lastUsed = entity.lastUsed
    )
}

fun HashtagResponse.toDomain(): Hashtag = HashtagMapper.toDomain(this)
fun Hashtag.toDto(): HashtagResponse = HashtagMapper.toDto(this)
