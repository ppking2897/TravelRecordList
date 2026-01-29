package com.example.myapplication.domain.interactor

import com.example.myapplication.domain.entity.Photo
import com.example.myapplication.domain.usecase.AddPhotoUseCase
import com.example.myapplication.domain.usecase.DeletePhotoUseCase
import com.example.myapplication.domain.usecase.GenerateThumbnailUseCase
import com.example.myapplication.domain.usecase.SetCoverPhotoUseCase

/**
 * 照片相關操作的 Interactor
 *
 * 封裝所有照片管理的 UseCase，簡化 ViewModel 的依賴
 */
class PhotoInteractor(
    private val addPhotoUseCase: AddPhotoUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val setCoverPhotoUseCase: SetCoverPhotoUseCase,
    private val generateThumbnailUseCase: GenerateThumbnailUseCase
) {
    /**
     * 新增照片到項目
     */
    suspend fun addPhoto(itemId: String, imageData: ByteArray): Result<Photo> {
        return addPhotoUseCase(itemId, imageData)
    }

    /**
     * 刪除照片
     */
    suspend fun deletePhoto(photoId: String): Result<Unit> {
        return deletePhotoUseCase(photoId)
    }

    /**
     * 設定項目的封面照片
     */
    suspend fun setCoverPhoto(itemId: String, photoId: String): Result<Unit> {
        return setCoverPhotoUseCase(itemId, photoId)
    }

    /**
     * 產生照片縮圖
     */
    suspend fun generateThumbnail(photo: Photo): Result<Photo> {
        return generateThumbnailUseCase(photo)
    }
}
