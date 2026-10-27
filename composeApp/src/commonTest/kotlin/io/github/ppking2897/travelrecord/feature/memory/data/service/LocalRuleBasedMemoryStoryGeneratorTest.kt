@file:OptIn(ExperimentalTime::class)

package io.github.ppking2897.travelrecord.feature.memory.data.service

import io.github.ppking2897.travelrecord.test.testItem
import io.github.ppking2897.travelrecord.test.testItinerary
import io.github.ppking2897.travelrecord.test.testPhoto
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.time.ExperimentalTime

/**
 * 回憶故事的文字，測試名稱對應 .claude/docs/SpecAdjustments.md 的 Scenario
 */
class LocalRuleBasedMemoryStoryGeneratorTest {

    private val generator = LocalRuleBasedMemoryStoryGenerator()

    @Test
    fun `回憶故事沒有多餘空格`() = runTest {
        val itinerary = testItinerary("kyoto").copy(
            startDate = LocalDate(2026, 10, 14),
            endDate = LocalDate(2026, 10, 16)
        )
        val items = listOf(
            visited("fushimi", LocalDate(2026, 10, 14), LocalTime(9, 0), "伏見稻荷", photos = 1),
            visited("kyoto-1", LocalDate(2026, 10, 14), LocalTime(15, 0), "京都"),
            visited("kyoto-2", LocalDate(2026, 10, 15), LocalTime(10, 0), "京都", photos = 1),
            visited("kinkaku", LocalDate(2026, 10, 15), LocalTime(14, 0), "金閣寺"),
            visited("arashiyama", LocalDate(2026, 10, 16), LocalTime(10, 0), "嵐山"),
        )

        val story = generator.generate(itinerary, items)

        story.title shouldBe "秋天的京都"
        story.summary shouldBe "10 月的京都之旅，走了 3 天、5 個地方。"
        story.narrative shouldBe "10 月 14 日，旅程從伏見稻荷展開。這 3 天裡走過 5 個地方，每一處都留下了打卡紀錄。" +
            "最後一站嵐山成了旅程的句點——相簿裡留下 2 張照片，剩下的就放在心裡。"
    }

    /** 已打卡的項目，照片數依參數 */
    private fun visited(id: String, date: LocalDate, arrival: LocalTime, place: String, photos: Int = 0) =
        testItem(id, "kyoto", date = date, arrivalTime = arrival, locationName = place).copy(
            isCompleted = true,
            photos = (0 until photos).map { testPhoto("$id-photo-$it", id, order = it) }
        )
}
