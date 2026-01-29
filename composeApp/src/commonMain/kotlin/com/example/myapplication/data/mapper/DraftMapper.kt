package com.example.myapplication.data.mapper

import com.example.myapplication.data.dto.DraftDto
import com.example.myapplication.data.dto.DraftTypeDto
import com.example.myapplication.domain.entity.Draft
import com.example.myapplication.domain.entity.DraftType

/**
 * DraftType DTO ↔ Entity 轉換
 */
object DraftTypeMapper {
    fun toEntity(dto: DraftTypeDto): DraftType = when (dto) {
        DraftTypeDto.ITINERARY -> DraftType.ITINERARY
        DraftTypeDto.ITEM -> DraftType.ITEM
    }

    fun toDto(entity: DraftType): DraftTypeDto = when (entity) {
        DraftType.ITINERARY -> DraftTypeDto.ITINERARY
        DraftType.ITEM -> DraftTypeDto.ITEM
    }
}

fun DraftTypeDto.toEntity(): DraftType = DraftTypeMapper.toEntity(this)
fun DraftType.toDto(): DraftTypeDto = DraftTypeMapper.toDto(this)

/**
 * Draft DTO ↔ Entity 轉換
 */
object DraftMapper {
    fun toEntity(dto: DraftDto): Draft = Draft(
        id = dto.id,
        type = dto.type.toEntity(),
        data = dto.data,
        createdAt = dto.createdAt,
        modifiedAt = dto.modifiedAt
    )

    fun toDto(entity: Draft): DraftDto = DraftDto(
        id = entity.id,
        type = entity.type.toDto(),
        data = entity.data,
        createdAt = entity.createdAt,
        modifiedAt = entity.modifiedAt
    )
}

fun DraftDto.toEntity(): Draft = DraftMapper.toEntity(this)
fun Draft.toDto(): DraftDto = DraftMapper.toDto(this)
