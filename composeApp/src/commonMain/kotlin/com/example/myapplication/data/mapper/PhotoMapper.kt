package com.example.myapplication.data.mapper

import com.example.myapplication.data.dto.PhotoDto
import com.example.myapplication.domain.entity.Photo

/**
 * Photo DTO ↔ Entity 轉換
 */
object PhotoMapper {
    fun toEntity(dto: PhotoDto): Photo = Photo(
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

fun PhotoDto.toEntity(): Photo = PhotoMapper.toEntity(this)
fun Photo.toDto(): PhotoDto = PhotoMapper.toDto(this)
