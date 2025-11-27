package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.ItineraryItemRepository
import kotlin.time.ExperimentalTime

/**
 * 刪除行程項目的 Use Case
 */
@ExperimentalTime
class DeleteItineraryItemUseCase(
    private val itemRepository: ItineraryItemRepository
) {
    suspend operator fun invoke(itemId: String): Result<Unit> {
        return itemRepository.deleteItem(itemId)
    }
}
