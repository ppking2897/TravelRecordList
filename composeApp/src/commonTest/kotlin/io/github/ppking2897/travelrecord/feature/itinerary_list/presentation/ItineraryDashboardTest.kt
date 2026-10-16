@file:OptIn(ExperimentalTime::class)

package io.github.ppking2897.travelrecord.feature.itinerary_list.presentation

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.test.testItem
import io.github.ppking2897.travelrecord.test.testItinerary
import io.github.ppking2897.travelrecord.test.testPhoto
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.time.ExperimentalTime

/**
 * 行程列表畫面的計算（篩選、區塊、摘要卡、卡片內容），測試名稱對應 .claude/docs/ItineraryList.md 的 Scenario
 */
class ItineraryDashboardTest {

    /** 今天固定是 2026-10-16 */
    private val today = LocalDate(2026, 10, 16)

    private val kyoto = trip("kyoto", "京都大阪 5 日遊", LocalDate(2026, 10, 14), LocalDate(2026, 10, 18))
    private val tokyo = trip("tokyo", "東京 3 日遊", LocalDate(2026, 9, 15), LocalDate(2026, 9, 17))

    @Test
    fun `篩選旅行中`() {
        val dashboard = buildItineraryDashboard(listOf(kyoto, tokyo), "", ItineraryFilter.InProgress, today)

        dashboard.allTrips.map { it.title } shouldBe listOf("京都大阪 5 日遊")
    }

    @Test
    fun `摘要卡數字`() {
        val planning = (1..5).map { trip("plan-$it", "規劃 $it", LocalDate(2026, 11, it), LocalDate(2026, 11, it + 2)) }

        val dashboard = buildItineraryDashboard(listOf(kyoto, tokyo) + planning, "", ItineraryFilter.All, today)

        dashboard.currentCount shouldBe 1
        dashboard.upcomingCount shouldBe 5
        dashboard.completedCount shouldBe 1
        dashboard.upcomingTrips shouldHaveSize 3
    }

    @Test
    fun `即將出發依開始日排序、最多 3 筆`() {
        val trips = listOf(
            trip("nov20", "11/20 出發", LocalDate(2026, 11, 20), null),
            trip("nov01", "11/01 出發", LocalDate(2026, 11, 1), null),
            trip("dec05", "12/05 出發", LocalDate(2026, 12, 5), null),
            trip("undated", "還沒排日期", null, null),
            trip("nov10", "11/10 出發", LocalDate(2026, 11, 10), null),
        )

        val dashboard = buildItineraryDashboard(trips, "", ItineraryFilter.All, today)

        dashboard.upcomingTrips.map { it.id } shouldBe listOf("nov01", "nov10", "nov20")
    }

    @Test
    fun `只有一筆行程時不顯示最近編輯`() {
        val dashboard = buildItineraryDashboard(listOf(kyoto), "", ItineraryFilter.All, today)

        dashboard.recentTrips.shouldBeEmpty()
    }

    @Test
    fun `搜尋沒有結果`() {
        val dashboard = buildItineraryDashboard(listOf(kyoto, tokyo), "巴黎", ItineraryFilter.All, today)

        dashboard.emptyState shouldBe DashboardEmptyState.NoMatch
    }

    /** 畫面拿到的是 ViewModel 搜尋後的清單，搜不到時清單本身就是空的 */
    @Test
    fun `搜尋後清單已經是空的`() {
        val dashboard = buildItineraryDashboard(emptyList(), "巴黎", ItineraryFilter.All, today)

        dashboard.emptyState shouldBe DashboardEmptyState.NoMatch
    }

    @Test
    fun `一筆行程都沒有`() {
        val dashboard = buildItineraryDashboard(emptyList(), "", ItineraryFilter.All, today)

        dashboard.emptyState shouldBe DashboardEmptyState.NoItineraries
    }

    @Test
    fun `卡片統計`() {
        val itinerary = kyoto.copy(
            items = listOf(
                testItem("a", "kyoto").copy(isCompleted = true, photos = listOf(testPhoto("p1", "a"), testPhoto("p2", "a"))),
                testItem("b", "kyoto").copy(photos = listOf(testPhoto("p3", "b"))),
                testItem("c", "kyoto").copy(photos = listOf(testPhoto("p4", "c"))),
            )
        )

        itinerary.cardStats() shouldBe ItineraryCardStats(itemCount = 3, completedCount = 1, photoCount = 4)
    }

    @Test
    fun `出現最多次的地點`() {
        val items = listOf(
            testItem("a", "kyoto", locationName = "清水寺"),
            testItem("b", "kyoto", locationName = "金閣寺"),
            testItem("c", "kyoto", locationName = "清水寺"),
        )

        mainLocationOf(items) shouldBe "清水寺"
    }

    @Test
    fun `只填一個日期`() {
        formatDateRange(LocalDate(2026, 11, 1), null) shouldBe "開始：2026/11/01"
        formatDateRange(null, null) shouldBe null
    }

    private fun trip(id: String, title: String, start: LocalDate?, end: LocalDate?): Itinerary =
        testItinerary(id = id, title = title).copy(startDate = start, endDate = end)
}
