package io.github.ppking2897.travelrecord.feature.route.data.repository

import io.github.ppking2897.travelrecord.core.data.local.InMemoryStorageService
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
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
 * 排序規則對應產品規格書「路線頁 → 路線怎麼產生」：依日期、時間排序，同名地點只留第一次。
 */
class RouteRepositoryImplTest {

    private val oct14 = LocalDate(2026, 10, 14)
    private val oct15 = LocalDate(2026, 10, 15)
    private val oct16 = LocalDate(2026, 10, 16)
    private val kyoto = testItinerary("kyoto", title = "京都 3 日遊")

    private val storage = InMemoryStorageService()
    private val items = InMemoryItineraryItemRepository()
    private val routes = RouteRepositoryImpl(storage, InMemoryItineraryRepository(items, kyoto))

    private fun item(id: String, location: String, date: LocalDate, arrival: LocalTime? = null) =
        testItem(id, "kyoto", date = date, arrivalTime = arrival, locationName = location)

    private suspend fun add(vararg newItems: ItineraryItem) {
        newItems.forEach { items.addItem(it).getOrThrow() }
    }

    private suspend fun RouteRepositoryImpl.locationNames() =
        getRouteForItinerary("kyoto").getOrThrow().locations.map { it.location.name }

    /** 讀行程時項目照給的順序回傳，不先排好，才看得出路線自己有沒有排序 */
    private fun unsortedTrip(vararg tripItems: ItineraryItem): ItineraryRepository =
        object : ItineraryRepository by stubItineraryRepository() {
            override suspend fun getItinerary(id: String) = Result.success(kyoto.copy(items = tripItems.toList()))
        }

    @Test
    fun `依日期與時間排序，同名地點只留第一次`() = runTest {
        val trip = unsortedTrip(
            item("kinkaku", "金閣寺", oct15, LocalTime(10, 0)),
            item("hotel-2", "飯店", oct16, LocalTime(20, 0)),
            item("fushimi", "伏見稻荷", oct14, LocalTime(15, 0)),
            item("hotel-1", "飯店", oct14, LocalTime(8, 0)),
        )

        val route = RouteRepositoryImpl(storage, trip).getRouteForItinerary("kyoto").getOrThrow()

        route.locations.map { it.location.name } shouldBe listOf("飯店", "伏見稻荷", "金閣寺")
        route.locations.map { it.order } shouldBe listOf(0, 1, 2)
        route.title shouldBe "京都 3 日遊"
        route.createdFrom shouldBe "kyoto"
    }

    @Test
    fun `路線沒排時間的排在當天最後`() = runTest {
        val trip = unsortedTrip(
            item("gion", "祇園", oct14),
            item("fushimi", "伏見稻荷", oct14, LocalTime(9, 0)),
            item("kinkaku", "金閣寺", oct15, LocalTime(10, 0)),
        )

        val route = RouteRepositoryImpl(storage, trip).getRouteForItinerary("kyoto").getOrThrow()

        route.locations.map { it.location.name } shouldBe listOf("伏見稻荷", "祇園", "金閣寺")
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
}
