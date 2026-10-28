package io.github.ppking2897.travelrecord.feature.route.domain.model

/**
 * 依天分段的旅遊路線領域模型
 *
 * @property title 標題
 * @property days 每天的路線，只有有項目的天
 * @property createdFrom 來源行程 ID
 */
data class Route(
    val title: String,
    val days: List<RouteDay>,
    val createdFrom: String
) {
    /** 不同地點的數量，同一個地點去好幾次只算一個 */
    val placeCount: Int
        get() = days.flatMap { it.stops }.map { it.location.name }.distinct().size
}
