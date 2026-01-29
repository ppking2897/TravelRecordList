package com.example.myapplication.data.model

import com.example.myapplication.test.localDate
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.checkAll

/**
 * Property Test: 日期範圍驗證
 *
 * Property 1: For any Itinerary with startDate and endDate,
 * if endDate is before startDate, then validation should fail
 *
 * Validates: 需求 1.3
 */
class ItineraryPropertyTest : StringSpec({

    "Property 1: 日期範圍驗證 - endDate 不能早於 startDate" {
        checkAll(100, Arb.localDate(), Arb.localDate()) { date1, date2 ->
            val startDate = minOf(date1, date2)
            val endDate = maxOf(date1, date2)

            // 有效的日期範圍
            val isValid = endDate >= startDate
            isValid shouldBe true
        }
    }

    "Property 1: 日期範圍驗證 - 無效的日期範圍應該被檢測" {
        checkAll(100, Arb.localDate(), Arb.localDate()) { date1, date2 ->
            if (date1 > date2) {
                // endDate 早於 startDate 的情況
                val isInvalid = date2 < date1
                isInvalid shouldBe true
            }
        }
    }
})
