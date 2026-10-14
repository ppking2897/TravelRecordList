package com.example.myapplication.core.presentation.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * 集中管理 UI 層的日期/時間格式化邏輯，取代各 Screen 散落的 private formatTime /
 * formatDate / LocalDate.toDisplayString。
 */
object DateTimeFormatter {

    /**
     * 格式化為 `yyyy/MM/dd`。null 回傳空字串。
     */
    fun formatDate(date: LocalDate?): String {
        if (date == null) return ""
        val month = date.monthNumber.toString().padStart(2, '0')
        val day = date.dayOfMonth.toString().padStart(2, '0')
        return "${date.year}/$month/$day"
    }

    /**
     * 格式化為 `HH:mm`（24 小時制，無秒、無毫秒）。null 回傳空字串。
     */
    fun formatTime(time: LocalTime?): String {
        if (time == null) return ""
        val hour = time.hour.toString().padStart(2, '0')
        val minute = time.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }
}
