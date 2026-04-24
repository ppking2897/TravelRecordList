package com.example.myapplication.feature.itinerary_item.domain.usecase

import com.example.myapplication.feature.itinerary_item.domain.model.ItineraryItem
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * 項目按日期分組的資料類別
 */
data class ItemsByDate(
    val date: LocalDate,
    val items: List<ItineraryItem>
)

/**
 * 將行程項目按日期分組並排序的 Use Case
 *
 * 需求: 3.3, 3.4
 */
class GroupItemsByDateUseCase {
    /**
     * 將項目列表按日期分組
     *
     * @param items 要分組的項目列表
     * @return 按日期分組的項目列表，每組內按時間排序
     */
    operator fun invoke(items: List<ItineraryItem>): List<ItemsByDate> {
        return items
            .groupBy { it.date }
            .map { (date, groupItems) ->
                ItemsByDate(
                    date = date,
                    items = groupItems.sortedWith(
                        // 優先依使用者自訂順序（order），相同 order 再依時間排序
                        // 沒有時間的項目排在最後
                        compareBy<ItineraryItem> { it.order }
                            .thenBy { it.primaryTime() ?: LocalTime(23, 59, 59) }
                    )
                )
            }
            .sortedBy { it.date }
    }
}
