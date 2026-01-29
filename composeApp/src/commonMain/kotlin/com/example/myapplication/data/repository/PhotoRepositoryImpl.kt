@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.data.repository

import com.example.myapplication.data.mapper.toDto
import com.example.myapplication.data.storage.ImageStorageService
import com.example.myapplication.domain.entity.Itinerary
import com.example.myapplication.domain.entity.ItineraryItem
import com.example.myapplication.domain.entity.Photo
import com.example.myapplication.domain.repository.ItineraryItemRepository
import com.example.myapplication.domain.repository.ItineraryRepository
import com.example.myapplication.domain.repository.PhotoRepository
import kotlin.time.Clock

/**
 * PhotoRepository 的實作
 */
class PhotoRepositoryImpl(
    private val imageStorageService: ImageStorageService,
    private val itineraryRepository: ItineraryRepository,
    private val itineraryItemRepository: ItineraryItemRepository
) : PhotoRepository {

    override suspend fun addPhoto(itemId: String, imageData: ByteArray): Result<Photo> {
        return try {
            val imagePath = imageStorageService.saveImage(imageData, itemId).getOrThrow()

            val thumbnailPath = try {
                val thumbnailData = imageStorageService.generateThumbnail(imageData).getOrThrow()
                imageStorageService.saveImage(thumbnailData, "${itemId}_thumb").getOrThrow()
            } catch (e: Exception) {
                null
            }

            val now = Clock.System.now()
            val photo = Photo(
                id = generatePhotoId(),
                itemId = itemId,
                fileName = imagePath.substringAfterLast('/'),
                filePath = imagePath,
                thumbnailPath = thumbnailPath,
                order = 0,
                isCover = false,
                width = null,
                height = null,
                fileSize = imageData.size.toLong(),
                uploadedAt = now,
                modifiedAt = now
            )

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
            val (itinerary, item) = findItemByPhotoId(photoId).getOrThrow()

            val photo = item.photos.find { it.id == photoId }
                ?: return Result.failure(Exception("Photo not found: $photoId"))

            imageStorageService.deleteImage(photo.filePath).getOrThrow()

            photo.thumbnailPath?.let { thumbPath ->
                imageStorageService.deleteImage(thumbPath)
            }

            val updatedPhotos = item.photos.filter { it.id != photoId }

            val updatedCoverPhotoId = if (item.coverPhotoId == photoId) null else item.coverPhotoId

            val updatedItem = item.copy(
                photos = updatedPhotos,
                coverPhotoId = updatedCoverPhotoId
            )

            val updatedItinerary = itinerary.copy(
                items = itinerary.items.map { if (it.id == item.id) updatedItem else it }
            )

            itineraryRepository.updateItinerary(updatedItinerary).getOrThrow()
            itineraryItemRepository.updateItem(updatedItem).getOrThrow()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setCoverPhoto(itemId: String, photoId: String): Result<Unit> {
        return try {
            val (itinerary, item) = findItemById(itemId).getOrThrow()

            if (!item.photos.any { it.id == photoId }) {
                return Result.failure(Exception("Photo not found in item: $photoId"))
            }

            val updatedItem = item.copy(coverPhotoId = photoId)
            val updatedItinerary = itinerary.copy(
                items = itinerary.items.map { if (it.id == itemId) updatedItem else it }
            )

            itineraryRepository.updateItinerary(updatedItinerary).getOrThrow()
            itineraryItemRepository.updateItem(updatedItem).getOrThrow()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reorderPhotos(itemId: String, photoIds: List<String>): Result<Unit> {
        return try {
            updateItemPhotos(itemId) { photos ->
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

    override suspend fun generateAndSaveThumbnail(photo: Photo): Result<Photo> {
        return try {
            val originalData = imageStorageService.loadImage(photo.filePath).getOrThrow()

            val thumbnailData = imageStorageService.generateThumbnail(originalData).getOrThrow()

            val thumbnailPath = imageStorageService.saveImage(thumbnailData, "${photo.itemId}_thumb").getOrThrow()

            val updatedPhoto = photo.copy(
                thumbnailPath = thumbnailPath,
                modifiedAt = Clock.System.now()
            )

            updateItemPhotos(photo.itemId) { photos ->
                photos.map { if (it.id == photo.id) updatedPhoto else it }
            }.getOrThrow()

            Result.success(updatedPhoto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

            itineraryItemRepository.updateItem(updatedItem).getOrThrow()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun findItemById(itemId: String): Result<Pair<Itinerary, ItineraryItem>> {
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

    private suspend fun findItemByPhotoId(photoId: String): Result<Pair<Itinerary, ItineraryItem>> {
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

    private fun generatePhotoId(): String {
        return "photo_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
    }
}
