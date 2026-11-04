package io.github.ppking2897.travelrecord.feature.route.presentation

import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Location
import io.github.ppking2897.travelrecord.feature.route.domain.model.RouteDay
import io.github.ppking2897.travelrecord.feature.route.domain.model.RouteLocation
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.http.Url
import kotlinx.datetime.LocalDate
import kotlin.test.Test

/**
 * 路線頁開 Google 地圖的網址
 *
 * 情境對應 .claude/docs/DayRouteMap.md。網址照 Google Maps URLs 的 Directions 格式，
 * 斷言解開參數後比對，不比對整串字（參數順序、編碼大小寫不是需求）。
 */
class RouteGoogleMapsLinkTest {

    private val day = LocalDate(2026, 10, 14)

    private fun stop(order: Int, name: String, lat: Double? = null, lng: Double? = null, address: String? = null) =
        RouteLocation(Location(name = name, latitude = lat, longitude = lng, address = address), order)

    private val fushimi = stop(0, "伏見稻荷大社", 34.9671, 135.7727)
    private val kiyomizu = stop(1, "清水寺", 34.9949, 135.7850)
    private val kinkakuji = stop(2, "金閣寺", 35.0394, 135.7292)

    private fun dayOf(vararg stops: RouteLocation) = RouteDay(day, dayNumber = 1, stops = stops.toList())

    private fun params(url: String) = Url(url).let { parsed ->
        parsed.host shouldBe "www.google.com"
        parsed.encodedPath shouldBe "/maps/dir/"
        parsed.parameters["api"] shouldBe "1"
        parsed.parameters
    }

    // Scenario: 兩站以上的天可以開多站路線
    @Test
    fun 三站的天起點是第一站中途站是中間終點是最後一站且不指定交通方式() {
        val link = dayOf(fushimi, kiyomizu, kinkakuji).toGoogleMapsDayLink().shouldNotBeNull()

        val p = params(link.url)
        p["origin"] shouldBe "34.9671,135.7727"
        p["waypoints"] shouldBe "34.9949,135.785"
        p["destination"] shouldBe "35.0394,135.7292"
        p["travelmode"].shouldBeNull()
        link.truncated shouldBe false
    }

    @Test
    fun 兩站的天沒有中途站() {
        val link = dayOf(fushimi, kiyomizu).toGoogleMapsDayLink().shouldNotBeNull()

        val p = params(link.url)
        p["origin"] shouldBe "34.9671,135.7727"
        p["destination"] shouldBe "34.9949,135.785"
        p["waypoints"].shouldBeNull()
    }

    @Test
    fun 多站路線照當天清單的順序跟畫面一致() {
        val link = dayOf(kinkakuji, fushimi, kiyomizu).toGoogleMapsDayLink().shouldNotBeNull()

        val p = params(link.url)
        p["origin"] shouldBe "35.0394,135.7292"
        p["waypoints"] shouldBe "34.9671,135.7727"
        p["destination"] shouldBe "34.9949,135.785"
    }

    // Scenario: 只有一站的天沒有「當天路線」
    @Test
    fun 只有一站的天沒有多站路線() {
        dayOf(fushimi).toGoogleMapsDayLink().shouldBeNull()
    }

    // Scenario: 地點沒有座標時改用地址或名稱
    @Test
    fun 沒座標的站用地址連地址都沒有就用名稱() {
        val withAddress = stop(1, "錦市場", address = "京都市中京區錦小路通")
        val nameOnly = stop(2, "祇園 小路")

        val link = dayOf(fushimi, withAddress, nameOnly).toGoogleMapsDayLink().shouldNotBeNull()

        val p = params(link.url)
        p["origin"] shouldBe "34.9671,135.7727"
        p["waypoints"] shouldBe "京都市中京區錦小路通"
        p["destination"] shouldBe "祇園 小路"
    }

    @Test
    fun 只有緯度沒有經度的站視為沒有座標() {
        val halfCoordinate = stop(1, "清水寺", lat = 34.9949)

        val link = dayOf(fushimi, halfCoordinate).toGoogleMapsDayLink().shouldNotBeNull()

        params(link.url)["destination"] shouldBe "清水寺"
    }

    @Test
    fun 中途站之間用直線分隔() {
        val fourth = stop(3, "嵐山", 35.0094, 135.6668)

        val link = dayOf(fushimi, kiyomizu, kinkakuji, fourth).toGoogleMapsDayLink().shouldNotBeNull()

        params(link.url)["waypoints"] shouldBe "34.9949,135.785|35.0394,135.7292"
    }

    // Scenario: 超過 11 站只帶前 11 站並提示
    @Test
    fun 十三站的天只帶前十一站並標示被截斷() {
        val stops = (0 until 13).map { stop(it, "第${it + 1}站", 35.0 + it / 100.0, 135.0) }

        val link = dayOf(*stops.toTypedArray()).toGoogleMapsDayLink().shouldNotBeNull()

        val p = params(link.url)
        p["origin"] shouldBe "35.0,135.0"
        p["destination"] shouldBe "35.1,135.0"
        p["waypoints"]!!.split("|").size shouldBe 9
        link.truncated shouldBe true
    }

    @Test
    fun 剛好十一站不算被截斷() {
        val stops = (0 until 11).map { stop(it, "第${it + 1}站", 35.0 + it / 100.0, 135.0) }

        val link = dayOf(*stops.toTypedArray()).toGoogleMapsDayLink().shouldNotBeNull()

        params(link.url)["destination"] shouldBe "35.1,135.0"
        link.truncated shouldBe false
    }

    // Scenario: 相鄰兩站之間查大眾運輸
    @Test
    fun 怎麼去從前一站到後一站並指定大眾運輸() {
        val asakusa = stop(0, "淺草寺", 35.7148, 139.7967)
        val skytree = stop(1, "東京晴空塔", 35.7101, 139.8107)

        val p = params(googleMapsTransitUrl(asakusa, skytree))

        p["origin"] shouldBe "35.7148,139.7967"
        p["destination"] shouldBe "35.7101,139.8107"
        p["travelmode"] shouldBe "transit"
        p["waypoints"].shouldBeNull()
    }

    // Scenario: 「怎麼去」的地點沒有座標時改用地址或名稱
    @Test
    fun 怎麼去的終點沒有座標時用名稱() {
        val asakusa = stop(0, "淺草寺", 35.7148, 139.7967)
        val akihabara = stop(1, "秋葉原")

        val p = params(googleMapsTransitUrl(asakusa, akihabara))

        p["origin"] shouldBe "35.7148,139.7967"
        p["destination"] shouldBe "秋葉原"
    }

    // Scenario: 從目前位置到這一站
    @Test
    fun 從目前位置到這站不帶起點並指定大眾運輸() {
        val skytree = stop(1, "東京晴空塔", 35.7101, 139.8107)

        val p = params(googleMapsTransitFromHereUrl(skytree))

        p["origin"].shouldBeNull()
        p["destination"] shouldBe "35.7101,139.8107"
        p["travelmode"] shouldBe "transit"
        p["waypoints"].shouldBeNull()
    }

    // Scenario: 這一站沒有座標時改用地址或名稱
    @Test
    fun 從目前位置到沒有座標的站用名稱() {
        val akihabara = stop(1, "秋葉原")

        params(googleMapsTransitFromHereUrl(akihabara))["destination"] shouldBe "秋葉原"
    }

    // 技術設計：每站的值一律做 URL 參數編碼，中途站用 | 分隔並編碼成 %7C
    @Test
    fun 站名含特殊字元時網址不會被切斷() {
        val tricky = stop(1, "A&B #1+C\n二樓")
        val last = stop(2, "D=E")

        val link = dayOf(fushimi, tricky, last).toGoogleMapsDayLink().shouldNotBeNull()

        val p = params(link.url)
        p.names() shouldBe setOf("api", "origin", "waypoints", "destination")
        p["waypoints"] shouldBe "A&B #1+C\n二樓"
        p["destination"] shouldBe "D=E"
    }

    @Test
    fun 中途站分隔符在網址裡編碼成百分比寫法() {
        val fourth = stop(3, "嵐山", 35.0094, 135.6668)

        val url = dayOf(fushimi, kiyomizu, kinkakuji, fourth).toGoogleMapsDayLink().shouldNotBeNull().url

        url.contains("%7C") shouldBe true
        url.contains("|") shouldBe false
    }

    // Scenario: 地點沒有座標時改用地址或名稱（地址是空字串或只有空白，視為沒有地址）
    @Test
    fun 地址是空字串或空白時用名稱() {
        val empty = stop(1, "錦市場", address = "")
        val blank = stop(2, "祇園", address = "   ")

        val p = params(dayOf(fushimi, empty, blank).toGoogleMapsDayLink().shouldNotBeNull().url)

        p["waypoints"] shouldBe "錦市場"
        p["destination"] shouldBe "祇園"
    }

    // 2026-11-04 決定：站名裡的 | 換成空白，不然 Google 會拆成兩站
    @Test
    fun 站名裡的直線換成空白不會被拆成兩站() {
        val piped = stop(1, "A|B")
        val last = stop(2, "清水寺", 34.9949, 135.7850)

        val p = params(dayOf(fushimi, piped, last).toGoogleMapsDayLink().shouldNotBeNull().url)

        p["waypoints"] shouldBe "A B"
    }
}
