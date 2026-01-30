package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.repository.ItineraryItemRepository
import kotlin.time.ExperimentalTime

/**
 * 重新排序行程項目的 Use Case
 *
 * 用於拖曳排序功能，更新項目的順序
 */
@ExperimentalTime
class ReorderItineraryItemsUseCase(
    private val itemRepository: ItineraryItemRepository
) {
    /**
     * 重新排序行程項目
     *
     * @param itineraryId 行程 ID
     * @param itemIds 新順序的項目 ID 列表
     * @return 操作結果
     */
    suspend operator fun invoke(itineraryId: String, itemIds: List<String>): Result<Unit> {
        return try {
            itemRepository.reorderItems(itineraryId, itemIds)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
