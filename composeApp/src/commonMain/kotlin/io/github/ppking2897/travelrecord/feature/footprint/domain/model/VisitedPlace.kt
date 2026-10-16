package io.github.ppking2897.travelrecord.feature.footprint.domain.model

import kotlinx.datetime.LocalDate

/**
 * 足跡畫面上的單一地點紀錄
 *
 * 一趟旅行可能多次到訪同一地點，每筆去過的 ItineraryItem 會對應一筆
 * VisitedPlace（「去過」的判定見 GetVisitedPlacesUseCase）。只在地圖上顯示有座標的地點，但列表仍會列出所有地點。
 */
data class VisitedPlace(
    val itemId: String,
    val locationName: String,
    val activity: String,
    val date: LocalDate,
    val tripId: String,
    val tripTitle: String,
    val latitude: Double?,
    val longitude: Double?,
    val isCompleted: Boolean
)
