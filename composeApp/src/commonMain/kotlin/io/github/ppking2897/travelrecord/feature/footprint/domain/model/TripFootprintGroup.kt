package io.github.ppking2897.travelrecord.feature.footprint.domain.model

import kotlinx.datetime.LocalDate

/**
 * 以「一趟旅行」為單位的足跡群組，用在足跡畫面的列表。
 *
 * @property tripId 所屬行程 ID
 * @property tripTitle 行程標題（同時也是群組的顯示標籤）
 * @property startDate 行程開始日期（可能為 null）
 * @property endDate 行程結束日期（可能為 null）
 * @property places 這趟旅行裡已走過的地點，預期按日期順序排好
 * @property completedCount 已打卡地點數
 */
data class TripFootprintGroup(
    val tripId: String,
    val tripTitle: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val places: List<VisitedPlace>
) {
    val placeCount: Int get() = places.size
    val completedCount: Int get() = places.count { it.isCompleted }
}
