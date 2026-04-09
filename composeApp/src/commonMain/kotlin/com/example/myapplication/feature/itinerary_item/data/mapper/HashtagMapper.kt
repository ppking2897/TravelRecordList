package com.example.myapplication.feature.itinerary_item.data.mapper

import com.example.myapplication.feature.itinerary_item.data.dto.HashtagDto
import com.example.myapplication.feature.itinerary_item.domain.model.Hashtag

/**
 * Hashtag DTO ??Entity è½‰æ?
 */
object HashtagMapper {
    fun toDomain(dto: HashtagDto): Hashtag = Hashtag(
        tag = dto.tag,
        usageCount = dto.usageCount,
        firstUsed = dto.firstUsed,
        lastUsed = dto.lastUsed
    )

    fun toDto(entity: Hashtag): HashtagDto = HashtagDto(
        tag = entity.tag,
        usageCount = entity.usageCount,
        firstUsed = entity.firstUsed,
        lastUsed = entity.lastUsed
    )
}

fun HashtagDto.toDomain(): Hashtag = HashtagMapper.toDomain(this)
fun Hashtag.toDto(): HashtagDto = HashtagMapper.toDto(this)
