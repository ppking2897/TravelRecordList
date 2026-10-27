package io.github.ppking2897.travelrecord.util

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlin.test.Test

/**
 * 日期顯示格式，測試名稱對應 .claude/docs/SpecAdjustments.md 的 Scenario
 */
class DateExtensionsTest {

    @Test
    fun `新增項目的日期選單寫完整日期`() {
        LocalDate(2026, 10, 20).toFriendlyString() shouldBe "2026/10/20（二）"
    }
}
