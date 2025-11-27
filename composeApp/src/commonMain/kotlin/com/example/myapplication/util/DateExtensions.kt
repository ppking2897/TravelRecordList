package com.example.myapplication.util

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * 日期範圍擴展函數
 */

/**
 * 將日期範圍轉換為日期列表
 * 
 * @return 從開始日期到結束日期的所有日期列表（包含首尾）
 */
fun ClosedRange<LocalDate>.toDateList(): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()
    var current = start
    while (current <= endInclusive) {
        dates.add(current)
        current = current.plus(1, DateTimeUnit.DAY)
    }
    return dates
}

/**
 * 格式化日期為使用者友善的格式
 * 
 * @return 格式化的日期字串，例如："2024-01-15 (週一)"
 */
fun LocalDate.toFriendlyString(): String {
    val dayOfWeek = when (this.dayOfWeek.ordinal + 1) {
        1 -> "週一"
        2 -> "週二"
        3 -> "週三"
        4 -> "週四"
        5 -> "週五"
        6 -> "週六"
        7 -> "週日"
        else -> ""
    }
    return "$this ($dayOfWeek)"
}

/**
 * 格式化日期為簡短格式
 * 
 * @return 格式化的日期字串，例如："01/15"
 */
fun LocalDate.toShortString(): String {
    return "${monthNumber.toString().padStart(2, '0')}/${dayOfMonth.toString().padStart(2, '0')}"
}

/**
 * 檢查日期是否在範圍內
 * 
 * @param range 日期範圍
 * @return 如果日期在範圍內返回 true
 */
fun LocalDate.isInRange(range: ClosedRange<LocalDate>): Boolean {
    return this >= range.start && this <= range.endInclusive
}
