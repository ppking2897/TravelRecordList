package com.example.myapplication.feature.draft.data.mapper

import com.example.myapplication.feature.draft.data.dto.DraftDto
import com.example.myapplication.feature.draft.data.dto.DraftTypeDto
import com.example.myapplication.feature.draft.domain.model.Draft
import com.example.myapplication.feature.draft.domain.model.DraftType

/**
 * DraftType DTO ??Entity è½‰æ?
 */
object DraftTypeMapper {
    fun toDomain(dto: DraftTypeDto): DraftType = when (dto) {
        DraftTypeDto.ITINERARY -> DraftType.ITINERARY
        DraftTypeDto.ITEM -> DraftType.ITEM
    }

    fun toDto(entity: DraftType): DraftTypeDto = when (entity) {
        DraftType.ITINERARY -> DraftTypeDto.ITINERARY
        DraftType.ITEM -> DraftTypeDto.ITEM
    }
}

fun DraftTypeDto.toDomain(): DraftType = DraftTypeMapper.toDomain(this)
fun DraftType.toDto(): DraftTypeDto = DraftTypeMapper.toDto(this)

/**
 * Draft DTO ??Entity è½‰æ?
 */
object DraftMapper {
    fun toDomain(dto: DraftDto): Draft = Draft(
        id = dto.id,
        type = dto.type.toDomain(),
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

fun DraftDto.toDomain(): Draft = DraftMapper.toDomain(this)
fun Draft.toDto(): DraftDto = DraftMapper.toDto(this)
