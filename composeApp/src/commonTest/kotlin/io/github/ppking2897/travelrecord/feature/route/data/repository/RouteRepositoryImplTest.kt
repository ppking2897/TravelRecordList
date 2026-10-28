package io.github.ppking2897.travelrecord.feature.route.data.repository

import io.github.ppking2897.travelrecord.core.data.local.InMemoryStorageService
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.route.domain.model.Route
import io.github.ppking2897.travelrecord.test.InMemoryItineraryItemRepository
import io.github.ppking2897.travelrecord.test.InMemoryItineraryRepository
import io.github.ppking2897.travelrecord.test.stubItineraryRepository
import io.github.ppking2897.travelrecord.test.testItem
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test

/**
 * 路線從行程現算、不存檔；舊版每次生成都另存一份的路線可以清掉
 *
 * 依天分段的情境對應 .claude/docs/SpecAdjustments.md 第 5 組。
 */
class RouteRepositoryImplTest {

    private val oct14 = LocalDate(2026, 10, 14)
    private val oct15 = LocalDate(2026, 10, 15)
    private val oct16 = LocalDate(2026, 10, 16)
    private val oct17 = LocalDate(2026, 10, 17)
    private val kyoto = testItinerary("kyoto", title = "京都大阪 5 日遊").copy(startDate = oct14, endDate = LocalDate(2026, 10, 18))

    private val storage = InMemoryStorageService()
    private val items = InMemoryItineraryItemRepository()
    private val routes = RouteRepositoryImpl(storage, InMemoryItineraryRepository(items, kyoto))

    private fun item(id: String, location: String, date: LocalDate, arrival: LocalTime? = null) =
        testItem(id, "kyoto", date = date, arrivalTime = arrival, locationName = location)

    private suspend fun add(vararg newItems: ItineraryItem) {
        newItems.forEach { items.addItem(it).getOrThrow() }
    }

    /** 整條路線依序經過的地點，不分天 */
    private suspend fun RouteRepositoryImpl.locationNames() =
        getRouteForItinerary("kyoto").getOrThrow().days.flatMap { day -> day.stops.map { it.location.name } }

    /** 讀行程時項目照給的順序回傳，不先排好，才看得出路線自己有沒有排序 */
    private fun unsortedTrip(vararg tripItems: ItineraryItem, trip: Itinerary = kyoto): ItineraryRepository =
        object : ItineraryRepository by stubItineraryRepository() {
            override suspend fun getItinerary(id: String) = Result.success(trip.copy(items = tripItems.toList()))
        }

    private suspend fun routeOf(vararg tripItems: ItineraryItem, trip: Itinerary = kyoto): Route =
        RouteRepositoryImpl(storage, unsortedTrip(*tripItems, trip = trip)).getRouteForItinerary("kyoto").getOrThrow()

    /** 每一段：第幾天 → 當天依序的地點 */
    private val Route.dayStops: List<Pair<Int?, List<String>>>
        get() = days.map { day -> day.dayNumber to day.stops.map { it.location.name } }

    @Test
    fun `路線沒排時間的排在當天最後`() = runTest {
        val route = routeOf(
            item("gion", "祇園", oct14),
            item("fushimi", "伏見稻荷", oct14, LocalTime(9, 0)),
            item("kinkaku", "金閣寺", oct15, LocalTime(10, 0)),
        )

        route.dayStops shouldBe listOf(1 to listOf("伏見稻荷", "祇園"), 2 to listOf("金閣寺"))
    }

    @Test
    fun `生成路線不會存檔`() = runTest {
        add(item("fushimi", "伏見稻荷", oct14), item("kinkaku", "金閣寺", oct15))

        routes.getRouteForItinerary("kyoto").getOrThrow()
        routes.getRouteForItinerary("kyoto").getOrThrow()

        storage.getAllKeys().getOrThrow().shouldBeEmpty()
    }

    @Test
    fun `路線跟著行程目前的項目`() = runTest {
        add(item("fushimi", "伏見稻荷", oct14), item("kinkaku", "金閣寺", oct15))
        routes.locationNames() shouldBe listOf("伏見稻荷", "金閣寺")

        add(item("arashiyama", "嵐山", oct16))

        routes.locationNames() shouldBe listOf("伏見稻荷", "金閣寺", "嵐山")
    }

    @Test
    fun `清掉舊版存下的路線，其他資料不動`() = runTest {
        storage.save("route:a1", "{}").getOrThrow()
        storage.save("route:b2", "{}").getOrThrow()
        storage.save("route:index", """["a1","b2"]""").getOrThrow()
        storage.save("draft_itinerary", "{}").getOrThrow()
        storage.save("itinerary:my-trip", "{}").getOrThrow()

        routes.deleteStoredRoutes().getOrThrow()

        storage.getAllKeys().getOrThrow() shouldContainExactlyInAnyOrder listOf("draft_itinerary", "itinerary:my-trip")
    }

    // ── 規格調整第 5 組：路線分天 ──

    @Test
    fun `路線依天分段`() = runTest {
        val route = routeOf(
            item("kinkaku", "金閣寺", oct15, LocalTime(10, 0)),
            item("gion", "祇園", oct14, LocalTime(18, 0)),
            item("fushimi", "伏見稻荷", oct14, LocalTime(9, 0)),
        )

        route.days.map { it.dayNumber to it.date } shouldBe listOf(1 to oct14, 2 to oct15)
        route.dayStops shouldBe listOf(1 to listOf("伏見稻荷", "祇園"), 2 to listOf("金閣寺"))
        route.days.map { day -> day.stops.map { it.order } } shouldBe listOf(listOf(0, 1), listOf(0))
    }

    @Test
    fun `沒有項目的天不列`() = runTest {
        val route = routeOf(
            item("fushimi", "伏見稻荷", oct14),
            item("arashiyama", "嵐山", oct16),
        )

        route.days.map { it.dayNumber } shouldBe listOf(1, 3)
    }

    @Test
    fun `不同天去同一個地點都會列出`() = runTest {
        val route = routeOf(
            item("hotel-1", "飯店", oct14, LocalTime(8, 0)),
            item("fushimi", "伏見稻荷", oct14, LocalTime(10, 0)),
            item("kinkaku", "金閣寺", oct17, LocalTime(10, 0)),
            item("hotel-2", "飯店", oct17, LocalTime(20, 0)),
        )

        route.dayStops shouldBe listOf(1 to listOf("飯店", "伏見稻荷"), 4 to listOf("金閣寺", "飯店"))
    }

    @Test
    fun `同一天連續在同一個地點只列一次`() = runTest {
        val route = routeOf(
            testItem("visit", "kyoto", date = oct15, arrivalTime = LocalTime(10, 0), locationName = "金閣寺", activity = "參觀"),
            testItem("photo", "kyoto", date = oct15, arrivalTime = LocalTime(11, 0), locationName = "金閣寺", activity = "拍照"),
            item("arashiyama", "嵐山", oct15, LocalTime(14, 0)),
        )

        route.dayStops shouldBe listOf(2 to listOf("金閣寺", "嵐山"))
        route.days.single().stops.map { it.order } shouldBe listOf(0, 1)
    }

    @Test
    fun `同一天離開再回來會列兩次`() = runTest {
        val route = routeOf(
            item("hotel-out", "飯店", oct14, LocalTime(8, 0)),
            item("fushimi", "伏見稻荷", oct14, LocalTime(10, 0)),
            item("hotel-back", "飯店", oct14, LocalTime(20, 0)),
        )

        route.dayStops shouldBe listOf(1 to listOf("飯店", "伏見稻荷", "飯店"))
        route.days.single().stops.map { it.order } shouldBe listOf(0, 1, 2)
    }

    @Test
    fun `行程範圍外的項目用日期標示`() = runTest {
        val shortened = kyoto.copy(endDate = oct16)

        val route = routeOf(
            item("fushimi", "伏見稻荷", oct14),
            item("arashiyama", "嵐山", oct17),
            trip = shortened,
        )

        route.days.map { it.date to it.dayNumber } shouldBe listOf(oct14 to 1, oct17 to null)
    }

    @Test
    fun `資訊卡算不同地點的數量`() = runTest {
        val route = routeOf(
            item("hotel-1", "飯店", oct14, LocalTime(8, 0)),
            item("fushimi", "伏見稻荷", oct14, LocalTime(10, 0)),
            item("kinkaku", "金閣寺", oct17, LocalTime(10, 0)),
            item("hotel-2", "飯店", oct17, LocalTime(20, 0)),
        )

        route.placeCount shouldBe 3
    }
}
