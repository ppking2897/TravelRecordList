package io.github.ppking2897.travelrecord.feature.route.domain.model

import kotlinx.datetime.LocalDate

/**
 * 路線裡的一天
 *
 * @property date 日期
 * @property dayNumber 行程的第幾天，從開始日算 1；日期在行程範圍外、或行程沒有日期時是 null
 * @property stops 當天依序要去的地點，[RouteLocation.order] 從 0 起算
 */
data class RouteDay(
    val date: LocalDate,
    val dayNumber: Int?,
    val stops: List<RouteLocation>
)
