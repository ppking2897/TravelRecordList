package io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase

import io.github.ppking2897.travelrecord.core.domain.service.ImageStorageService
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.ItineraryItemRepository
import kotlin.time.ExperimentalTime

/**
 * 批量刪除行程項目的 Use Case
 *
 * 用於批量操作功能，一次刪除多個選中的項目
 */
@ExperimentalTime
class BatchDeleteItemsUseCase(
    private val itemRepository: ItineraryItemRepository,
    private val imageStorageService: ImageStorageService
) {
    /**
     * 批量刪除行程項目
     *
     * @param itemIds 要刪除的項目 ID 列表
     * @return 操作結果，包含成功刪除的數量
     */
    suspend operator fun invoke(itemIds: List<String>): Result<Int> {
        return try {
            var deletedCount = 0
            val errors = mutableListOf<String>()

            itemIds.forEach { itemId ->
                // 先讀出項目，紀錄刪掉後才知道要刪哪些照片檔
                val item = itemRepository.getItem(itemId).getOrNull()
                itemRepository.deleteItem(itemId)
                    .onSuccess {
                        deletedCount++
                        item?.let { imageStorageService.deletePhotoFiles(listOf(it)) }
                    }
                    .onFailure { errors.add(itemId) }
            }

            if (errors.isNotEmpty() && deletedCount == 0) {
                Result.failure(Exception("刪除失敗：無法刪除任何項目"))
            } else {
                Result.success(deletedCount)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
