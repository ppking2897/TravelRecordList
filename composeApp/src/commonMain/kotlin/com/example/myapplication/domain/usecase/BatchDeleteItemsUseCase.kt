package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.repository.ItineraryItemRepository
import kotlin.time.ExperimentalTime

/**
 * 批量刪除行程項目的 Use Case
 *
 * 用於批量操作功能，一次刪除多個選中的項目
 */
@ExperimentalTime
class BatchDeleteItemsUseCase(
    private val itemRepository: ItineraryItemRepository
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
                itemRepository.deleteItem(itemId)
                    .onSuccess { deletedCount++ }
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
