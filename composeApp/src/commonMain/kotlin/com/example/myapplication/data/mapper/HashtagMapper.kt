package com.example.myapplication.data.mapper

import com.example.myapplication.data.dto.HashtagDto
import com.example.myapplication.domain.entity.Hashtag

/**
 * Hashtag DTO ↔ Entity 轉換
 */
object HashtagMapper {
    fun toEntity(dto: HashtagDto): Hashtag = Hashtag(
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

fun HashtagDto.toEntity(): Hashtag = HashtagMapper.toEntity(this)
fun Hashtag.toDto(): HashtagDto = HashtagMapper.toDto(this)
