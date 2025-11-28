package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Photo
import com.example.myapplication.data.storage.ImageStorageService
import kotlinx.datetime.Clock

/**
 * PhotoRepository 的實作
 */
@OptIn(kotlin.time.ExperimentalTime::class)
class PhotoRepositoryImpl(
    private val imageStorageService: ImageStorageService,
    private val itineraryRepository: ItineraryRepository
) : PhotoRepository {
    
    override suspend fun addPhoto(itemId: String, imageData: ByteArray): Result<Photo> {
        return try {
            // 儲存圖片
            val imagePath = imageStorageService.saveImage(imageData, itemId).getOrThrow()
            
            // 建立 Photo 物件
            val now = kotlin.time.Clock.System.now()
            val photo = Photo(
                id = generatePhotoId(),
                itemId = itemId,
                fileName = imagePath.substringAfterLast('/'),
                filePath = imagePath,
                thumbnailPath = null,
                order = 0,
                isCover = false,
                width = null,
                height = null,
                fileSize = imageData.size.toLong(),
                uploadedAt = now,
                modifiedAt = now
            )
            
            // 更新 ItineraryItem 的 photos 列表
            updateItemPhotos(itemId) { photos ->
                photos + photo
            }.getOrThrow()
            
            Result.success(photo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deletePhoto(photoId: String): Result<Unit> {
        return try {
            // 找到包含此照片的 item
            val (itinerary, item) = findItemByPhotoId(photoId).getOrThrow()
            
            // 找到要刪除的照片
            val photo = item.photos.find { it.id == photoId }
                ?: return Result.failure(Exception("Photo not found: $photoId"))
            
            // 刪除圖片檔案
            imageStorageService.deleteImage(photo.filePath).getOrThrow()
            
            // 從 item 的 photos 列表中移除
            val updatedPhotos = item.photos.filter { it.id != photoId }
            
            // 如果刪除的是封面照片，清除 coverPhotoId
            val updatedCoverPhotoId = if (item.coverPhotoId == photoId) null else item.coverPhotoId
            
            // 更新 item
            val updatedItem = item.copy(
                photos = updatedPhotos,
                coverPhotoId = updatedCoverPhotoId
            )
            
            val updatedItinerary = itinerary.copy(
                items = itinerary.items.map { if (it.id == item.id) updatedItem else it }
            )
            
            itineraryRepository.updateItinerary(updatedItinerary).getOrThrow()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setCoverPhoto(itemId: String, photoId: String): Result<Unit> {
        return try {
            // 找到 itinerary 和 item
            val (itinerary, item) = findItemById(itemId).getOrThrow()
            
            // 驗證照片存在
            if (!item.photos.any { it.id == photoId }) {
                return Result.failure(Exception("Photo not found in item: $photoId"))
            }
            
            // 更新 coverPhotoId
            val updatedItem = item.copy(coverPhotoId = photoId)
            val updatedItinerary = itinerary.copy(
                items = itinerary.items.map { if (it.id == itemId) updatedItem else it }
            )
            
            itineraryRepository.updateItinerary(updatedItinerary).getOrThrow()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun reorderPhotos(itemId: String, photoIds: List<String>): Result<Unit> {
        return try {
            updateItemPhotos(itemId) { photos ->
                // 根據新的順序重新排列照片
                photoIds.mapNotNull { id -> photos.find { it.id == id } }
            }.getOrThrow()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getPhotosByItem(itemId: String): Result<List<Photo>> {
        return try {
            val (_, item) = findItemById(itemId).getOrThrow()
            Result.success(item.photos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun loadPhotoData(photoPath: String): Result<ByteArray> {
        return imageStorageService.loadImage(photoPath)
    }
    
    /**
     * 更新 ItineraryItem 的照片列表
     */
    private suspend fun updateItemPhotos(
        itemId: String,
        transform: (List<Photo>) -> List<Photo>
    ): Result<Unit> {
        return try {
            val (itinerary, item) = findItemById(itemId).getOrThrow()
            
            val updatedPhotos = transform(item.photos)
            val updatedItem = item.copy(photos = updatedPhotos)
            
            val updatedItinerary = itinerary.copy(
                items = itinerary.items.map { if (it.id == itemId) updatedItem else it }
            )
            
            itineraryRepository.updateItinerary(updatedItinerary).getOrThrow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根據 itemId 找到對應的 Itinerary 和 ItineraryItem
     */
    private suspend fun findItemById(itemId: String): Result<Pair<com.example.myapplication.data.model.Itinerary, com.example.myapplication.data.model.ItineraryItem>> {
        return try {
            val allItineraries = itineraryRepository.getAllItineraries().getOrThrow()
            
            for (itinerary in allItineraries) {
                val item = itinerary.items.find { it.id == itemId }
                if (item != null) {
                    return Result.success(itinerary to item)
                }
            }
            
            Result.failure(Exception("Item not found: $itemId"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根據 photoId 找到對應的 Itinerary 和 ItineraryItem
     */
    private suspend fun findItemByPhotoId(photoId: String): Result<Pair<com.example.myapplication.data.model.Itinerary, com.example.myapplication.data.model.ItineraryItem>> {
        return try {
            val allItineraries = itineraryRepository.getAllItineraries().getOrThrow()
            
            for (itinerary in allItineraries) {
                for (item in itinerary.items) {
                    if (item.photos.any { it.id == photoId }) {
                        return Result.success(itinerary to item)
                    }
                }
            }
            
            Result.failure(Exception("Photo not found: $photoId"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 生成唯一的照片 ID
     */
    private fun generatePhotoId(): String {
        return "photo_${kotlin.time.Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
    }
}
