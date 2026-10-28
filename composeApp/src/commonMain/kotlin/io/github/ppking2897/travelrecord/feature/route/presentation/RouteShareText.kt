package io.github.ppking2897.travelrecord.feature.route.presentation

import io.github.ppking2897.travelrecord.core.presentation.util.DateTimeFormatter
import io.github.ppking2897.travelrecord.feature.route.domain.model.Route

/**
 * 分享出去的路線文字：第一行是行程標題，之後每天一行「第 1 天：伏見稻荷 → 祇園」
 *
 * 日期在行程範圍外的那天，用日期 2026/10/17 代替「第 N 天」。
 */
fun Route.toShareText(): String {
    val dayLines = days.map { day ->
        val label = day.dayNumber?.let { "第 $it 天" } ?: DateTimeFormatter.formatDate(day.date)
        "$label：" + day.stops.joinToString(" → ") { it.location.name }
    }
    return (listOf(title) + dayLines).joinToString("\n")
}
