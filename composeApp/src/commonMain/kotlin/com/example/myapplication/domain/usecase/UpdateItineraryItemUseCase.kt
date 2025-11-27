package com.example.myapplication.domain.usecase

import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.data.model.Validation
import com.example.myapplication.data.repository.ItineraryItemRepository

/**
 * 更新行程項目的 Use Case
 */
@OptIn(kotlin.time.ExperimentalTime::class)
class UpdateItineraryItemUseCase(
    private val itemRepository: ItineraryItemRepository
) {
    suspend operator fun invoke(
        item: ItineraryItem,
        currentTimestamp: kotlinx.datetime.Instant
    ): Result<ItineraryItem> {
        return try {
            // 驗證輸入
            Validation.validateItineraryItem(item).getOrElse { return Result.failure(it) }
            
            // 更新 modifiedAt
            val updatedItem = item.copy(modifiedAt = currentTimestamp)
            
            // 儲存
            itemRepository.updateItem(updatedItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
