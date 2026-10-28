package io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model

import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.GroupItemsByDateUseCase
import io.github.ppking2897.travelrecord.test.testItem
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test

/**
 * 各畫面共用的項目排序，測試名稱對應 .claude/docs/SpecAdjustments.md 的 Scenario
 */
class ItemDayOrderTest {

    private val oct14 = LocalDate(2026, 10, 14)

    /** 依建立先後排列，還沒排序 */
    private val oct14Items = listOf(
        testItem("gion", "kyoto", date = oct14, locationName = "祇園晚餐"),
        testItem("kiyomizu", "kyoto", date = oct14, arrivalTime = LocalTime(14, 30), locationName = "清水寺"),
        testItem("nishiki", "kyoto", date = oct14, arrivalTime = LocalTime(14, 30), locationName = "錦市場"),
        testItem("fushimi", "kyoto", date = oct14, departureTime = LocalTime(9, 0), locationName = "伏見稻荷"),
        testItem("yasaka", "kyoto", date = oct14, locationName = "八坂神社"),
    )

    private val expected = listOf("伏見稻荷", "清水寺", "錦市場", "祇園晚餐", "八坂神社")

    @Test
    fun `同一天的排序規則`() {
        oct14Items.sortedWith(itemDayOrder).map { it.location.name } shouldBe expected
    }

    @Test
    fun `行程詳情的分組照同一套排序`() {
        GroupItemsByDateUseCase()(oct14Items).single().items.map { it.location.name } shouldBe expected
    }
}
