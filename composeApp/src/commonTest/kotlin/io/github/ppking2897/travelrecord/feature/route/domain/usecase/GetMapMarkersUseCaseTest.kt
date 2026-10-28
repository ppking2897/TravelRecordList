package io.github.ppking2897.travelrecord.feature.route.domain.usecase

import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.test.StubItineraryItemRepository
import io.github.ppking2897.travelrecord.test.testItem
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test

/**
 * 地圖頁的地點順序，測試名稱對應 .claude/docs/SpecAdjustments.md 的 Scenario
 */
class GetMapMarkersUseCaseTest {

    private val oct14 = LocalDate(2026, 10, 14)
    private val oct15 = LocalDate(2026, 10, 15)

    private fun item(id: String, place: String, date: LocalDate, arrival: LocalTime? = null): ItineraryItem {
        val base = testItem(id, "kyoto", date = date, arrivalTime = arrival, locationName = place)
        return base.copy(location = base.location.copy(latitude = 35.0, longitude = 135.7))
    }

    @Test
    fun `地圖頁沒排時間的排在當天最後`() = runTest {
        // 讀項目時照建立先後回傳、不先排好，才看得出地圖自己有沒有排序
        val items = StubItineraryItemRepository(
            listOf(
                item("gion", "祇園", oct14),
                item("fushimi", "伏見稻荷", oct14, LocalTime(9, 0)),
                item("kinkaku", "金閣寺", oct15, LocalTime(10, 0)),
            )
        )

        val markers = GetMapMarkersUseCase(items)("kyoto").getOrThrow()

        markers.map { Triple(it.locationName, it.date, it.order) } shouldBe listOf(
            Triple("伏見稻荷", oct14, 1),
            Triple("祇園", oct14, 2),
            Triple("金閣寺", oct15, 1),
        )
    }
}
