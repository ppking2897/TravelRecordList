package io.github.ppking2897.travelrecord.feature.draft.data.mapper

import io.github.ppking2897.travelrecord.feature.draft.data.model.DraftResponse
import io.github.ppking2897.travelrecord.feature.draft.data.model.DraftTypeResponse
import io.github.ppking2897.travelrecord.feature.draft.domain.model.Draft
import io.github.ppking2897.travelrecord.feature.draft.domain.model.DraftType

/**
 * DraftType Response ↔ Domain 轉換
 */
object DraftTypeMapper {
    fun toDomain(dto: DraftTypeResponse): DraftType = when (dto) {
        DraftTypeResponse.ITINERARY -> DraftType.ITINERARY
        DraftTypeResponse.ITEM -> DraftType.ITEM
    }

    fun toDto(entity: DraftType): DraftTypeResponse = when (entity) {
        DraftType.ITINERARY -> DraftTypeResponse.ITINERARY
        DraftType.ITEM -> DraftTypeResponse.ITEM
    }
}

fun DraftTypeResponse.toDomain(): DraftType = DraftTypeMapper.toDomain(this)
fun DraftType.toDto(): DraftTypeResponse = DraftTypeMapper.toDto(this)

/**
 * Draft Response ↔ Domain 轉換
 */
object DraftMapper {
    fun toDomain(dto: DraftResponse): Draft = Draft(
        id = dto.id,
        type = dto.type.toDomain(),
        data = dto.data,
        createdAt = dto.createdAt,
        modifiedAt = dto.modifiedAt
    )

    fun toDto(entity: Draft): DraftResponse = DraftResponse(
        id = entity.id,
        type = entity.type.toDto(),
        data = entity.data,
        createdAt = entity.createdAt,
        modifiedAt = entity.modifiedAt
    )
}

fun DraftResponse.toDomain(): Draft = DraftMapper.toDomain(this)
fun Draft.toDto(): DraftResponse = DraftMapper.toDto(this)
