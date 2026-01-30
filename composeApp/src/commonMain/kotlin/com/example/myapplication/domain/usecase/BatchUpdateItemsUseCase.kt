package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.repository.ItineraryItemRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * 批量更新行程項目的 Use Case
 *
 * 用於批量操作功能，一次更新多個選中項目的狀態
 */
@ExperimentalTime
class BatchUpdateItemsUseCase(
    private val itemRepository: ItineraryItemRepository
) {
    /**
     * 批量標記項目為完成
     *
     * @param itemIds 要更新的項目 ID 列表
     * @return 操作結果，包含成功更新的數量
     */
    suspend fun markComplete(itemIds: List<String>): Result<Int> {
        return try {
            var updatedCount = 0
            val currentTime = Clock.System.now()

            itemIds.forEach { itemId ->
                itemRepository.getItem(itemId)
                    .onSuccess { item ->
                        if (item != null && !item.isCompleted) {
                            val updatedItem = item.copy(
                                isCompleted = true,
                                completedAt = currentTime,
                                modifiedAt = currentTime
                            )
                            itemRepository.updateItem(updatedItem)
                                .onSuccess { updatedCount++ }
                        } else if (item != null && item.isCompleted) {
                            // 已完成的項目也算成功
                            updatedCount++
                        }
                    }
            }

            Result.success(updatedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 批量標記項目為未完成
     *
     * @param itemIds 要更新的項目 ID 列表
     * @return 操作結果，包含成功更新的數量
     */
    suspend fun markIncomplete(itemIds: List<String>): Result<Int> {
        return try {
            var updatedCount = 0
            val currentTime = Clock.System.now()

            itemIds.forEach { itemId ->
                itemRepository.getItem(itemId)
                    .onSuccess { item ->
                        if (item != null && item.isCompleted) {
                            val updatedItem = item.copy(
                                isCompleted = false,
                                completedAt = null,
                                modifiedAt = currentTime
                            )
                            itemRepository.updateItem(updatedItem)
                                .onSuccess { updatedCount++ }
                        } else if (item != null && !item.isCompleted) {
                            // 未完成的項目也算成功
                            updatedCount++
                        }
                    }
            }

            Result.success(updatedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
