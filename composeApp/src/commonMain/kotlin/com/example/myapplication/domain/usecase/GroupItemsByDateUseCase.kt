package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.entity.ItineraryItem
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
                        compareBy<ItineraryItem> { 
                            // 沒有時間的項目排在最後
                            it.primaryTime() ?: LocalTime(23, 59, 59)
                        }
                    )
                )
            }
            .sortedBy { it.date }
    }
}
