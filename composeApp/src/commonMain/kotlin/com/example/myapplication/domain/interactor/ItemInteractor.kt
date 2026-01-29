@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.domain.interactor

import com.example.myapplication.domain.entity.ItineraryItem
import com.example.myapplication.domain.repository.ItineraryItemRepository
import com.example.myapplication.domain.usecase.DeleteItineraryItemUseCase
import com.example.myapplication.domain.usecase.FilterByHashtagUseCase
import com.example.myapplication.domain.usecase.FilterItemsByDateUseCase
import com.example.myapplication.domain.usecase.GroupItemsByDateUseCase
import com.example.myapplication.domain.usecase.ItemsByDate
import com.example.myapplication.domain.usecase.UpdateItineraryItemUseCase
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

/**
 * 行程項目相關操作的 Interactor
 *
 * 封裝項目的 CRUD、篩選、分組等操作
 */
class ItemInteractor(
    private val itemRepository: ItineraryItemRepository,
    private val updateItemUseCase: UpdateItineraryItemUseCase,
    private val deleteItemUseCase: DeleteItineraryItemUseCase,
    private val groupItemsByDateUseCase: GroupItemsByDateUseCase,
    private val filterItemsByDateUseCase: FilterItemsByDateUseCase,
    private val filterByHashtagUseCase: FilterByHashtagUseCase
) {
    /**
     * 取得行程的所有項目
     */
    suspend fun getItemsByItinerary(itineraryId: String): Result<List<ItineraryItem>> {
        return itemRepository.getItemsByItinerary(itineraryId)
    }

    /**
     * 更新項目
     */
    suspend fun updateItem(item: ItineraryItem): Result<ItineraryItem> {
        return updateItemUseCase(item, Clock.System.now())
    }

    /**
     * 切換項目完成狀態
     */
    suspend fun toggleCompletion(item: ItineraryItem): Result<ItineraryItem> {
        val updatedItem = item.copy(
            isCompleted = !item.isCompleted,
            completedAt = if (!item.isCompleted) Clock.System.now() else null
        )
        return updateItemUseCase(updatedItem, Clock.System.now())
    }

    /**
     * 刪除項目
     */
    suspend fun deleteItem(itemId: String): Result<Unit> {
        return deleteItemUseCase(itemId)
    }

    /**
     * 依日期分組項目
     * @return Map<LocalDate, List<ItineraryItem>> 方便 ViewModel 使用
     */
    fun groupByDate(items: List<ItineraryItem>): Map<LocalDate, List<ItineraryItem>> {
        val grouped: List<ItemsByDate> = groupItemsByDateUseCase(items)
        return grouped.associate { it.date to it.items }
    }

    /**
     * 依日期篩選項目
     */
    fun filterByDate(items: List<ItineraryItem>, date: LocalDate): List<ItineraryItem> {
        return filterItemsByDateUseCase(items, date)
    }

    /**
     * 依 Hashtag 篩選項目
     */
    fun filterByHashtag(items: List<ItineraryItem>, hashtag: String): List<ItineraryItem> {
        return filterByHashtagUseCase(items, hashtag)
    }
}
