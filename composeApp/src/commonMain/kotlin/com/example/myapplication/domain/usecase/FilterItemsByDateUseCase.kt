package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.entity.ItineraryItem
import kotlinx.datetime.LocalDate

/**
 * 按日期篩選行程項目的 Use Case
 * 
 * 需求: 2.3, 6.1
 */
class FilterItemsByDateUseCase {
    /**
     * 篩選指定日期的項目
     * 
     * @param items 要篩選的項目列表
     * @param selectedDate 選中的日期，null 表示顯示所有項目
     * @return 篩選後的項目列表
     */
    operator fun invoke(items: List<ItineraryItem>, selectedDate: LocalDate?): List<ItineraryItem> {
        return if (selectedDate == null) {
            items
        } else {
            items.filter { it.date == selectedDate }
        }
    }
}
