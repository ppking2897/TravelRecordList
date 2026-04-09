package com.example.myapplication.feature.itinerary_item.data.mapper

import com.example.myapplication.feature.itinerary_item.data.dto.PhotoDto
import com.example.myapplication.feature.itinerary_item.domain.model.Photo

/**
 * Photo DTO ??Entity è½‰æ?
 */
object PhotoMapper {
    fun toDomain(dto: PhotoDto): Photo = Photo(
        id = dto.id,
        itemId = dto.itemId,
        fileName = dto.fileName,
        filePath = dto.filePath,
        thumbnailPath = dto.thumbnailPath,
        order = dto.order,
        isCover = dto.isCover,
        width = dto.width,
        height = dto.height,
        fileSize = dto.fileSize,
        uploadedAt = dto.uploadedAt,
        modifiedAt = dto.modifiedAt
    )

    fun toDto(entity: Photo): PhotoDto = PhotoDto(
        id = entity.id,
        itemId = entity.itemId,
        fileName = entity.fileName,
        filePath = entity.filePath,
        thumbnailPath = entity.thumbnailPath,
        order = entity.order,
        isCover = entity.isCover,
        width = entity.width,
        height = entity.height,
        fileSize = entity.fileSize,
        uploadedAt = entity.uploadedAt,
        modifiedAt = entity.modifiedAt
    )
}

fun PhotoDto.toDomain(): Photo = PhotoMapper.toDomain(this)
fun Photo.toDto(): PhotoDto = PhotoMapper.toDto(this)
