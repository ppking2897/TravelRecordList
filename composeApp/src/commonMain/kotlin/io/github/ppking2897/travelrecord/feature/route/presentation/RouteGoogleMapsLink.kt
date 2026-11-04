package io.github.ppking2897.travelrecord.feature.route.presentation

import io.github.ppking2897.travelrecord.feature.route.domain.model.RouteDay
import io.github.ppking2897.travelrecord.feature.route.domain.model.RouteLocation
import io.ktor.http.URLBuilder

// TODO(等外部): iOS 開 Google 地圖網址未驗證，沒裝 Google 地圖 App 時落到手機瀏覽器只收 3 個中途站；移除條件：在 iOS 實機按「當天路線」「怎麼去」與每站的地圖按鈕，都能開啟 Google 地圖或瀏覽器並帶入各站，且確認瀏覽器時的站數上限與提示文字相符

/**
 * 「當天路線」（在 Google 地圖看這天）的網址
 *
 * @property truncated 這天超過 Google 地圖一次能帶的站數，只帶了前 [MAX_GOOGLE_MAPS_STOPS] 站
 */
data class DayMapsLink(val url: String, val truncated: Boolean)

/** Google 地圖網址一次最多 11 站：起點、9 個中途站、終點 */
const val MAX_GOOGLE_MAPS_STOPS = 11

/**
 * 這天依序所有地點的多站路線（照 [RouteDay.stops] 的順序，跟畫面一致），不指定交通方式；不到 2 站回 null
 *
 * 規格：.claude/docs/DayRouteMap.md「每天可以在 Google 地圖看當天的多站路線」
 */
fun RouteDay.toGoogleMapsDayLink(): DayMapsLink? {
    val ordered = stops
    if (ordered.size < 2) return null
    val used = ordered.take(MAX_GOOGLE_MAPS_STOPS)
    val url = directionsUrl(
        origin = used.first(),
        destination = used.last(),
        waypoints = used.subList(1, used.size - 1),
        travelMode = null,
    )
    return DayMapsLink(url = url, truncated = ordered.size > MAX_GOOGLE_MAPS_STOPS)
}

/**
 * 從 [from] 到 [to] 的大眾運輸路線
 *
 * 規格：.claude/docs/DayRouteMap.md「相鄰兩站之間可以查大眾運輸怎麼去」
 */
fun googleMapsTransitUrl(from: RouteLocation, to: RouteLocation): String =
    directionsUrl(origin = from, destination = to, waypoints = emptyList(), travelMode = "transit")

/**
 * 從使用者目前的位置到 [to] 的大眾運輸路線；不帶起點，Google 地圖會用裝置位置
 *
 * 規格：.claude/docs/DayRouteMap.md「每一站可以查從目前位置怎麼過去」
 */
fun googleMapsTransitFromHereUrl(to: RouteLocation): String =
    directionsUrl(origin = null, destination = to, waypoints = emptyList(), travelMode = "transit")

/** Google Maps URLs 的 Directions：https://developers.google.com/maps/documentation/urls/get-started */
private fun directionsUrl(
    origin: RouteLocation?,
    destination: RouteLocation,
    waypoints: List<RouteLocation>,
    travelMode: String?,
): String = URLBuilder("https://www.google.com/maps/dir/").apply {
    parameters.append("api", "1")
    origin?.let { parameters.append("origin", it.mapsQuery()) }
    parameters.append("destination", destination.mapsQuery())
    if (waypoints.isNotEmpty()) {
        parameters.append("waypoints", waypoints.joinToString("|") { it.mapsQuery() })
    }
    travelMode?.let { parameters.append("travelmode", it) }
}.buildString()

/** 有座標用座標（最準），否則用地址，再沒有就用名稱 */
private fun RouteLocation.mapsQuery(): String {
    val lat = location.latitude
    val lng = location.longitude
    return when {
        lat != null && lng != null -> "$lat,$lng"
        !location.address.isNullOrBlank() -> location.address
        else -> location.name
    }.replace('|', ' ') // | 是中途站的分隔符，留在站名裡 Google 會拆成兩站
}
