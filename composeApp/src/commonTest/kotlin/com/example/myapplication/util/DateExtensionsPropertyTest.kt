package com.example.myapplication.util

import com.example.myapplication.data.model.localDate
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.checkAll
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * Property Test: 日期範圍生成完整性
 * 
 * Property 2: For any valid date range (start to end), 
 * the generated date list should contain all dates from start to end inclusive with no gaps
 * 
 * Validates: 需求 2.2, 5.2
 */
class DateExtensionsPropertyTest : StringSpec({
    
    "Property 2: 日期範圍生成完整性 - 包含所有日期且無間隙" {
        checkAll(100, Arb.localDate(), Arb.localDate()) { date1, date2 ->
            val startDate = minOf(date1, date2)
            val endDate = maxOf(date1, date2)
            val range = startDate..endDate
            
            val dateList = range.toDateList()
            
            // 驗證首尾日期
            dateList.first() shouldBe startDate
            dateList.last() shouldBe endDate
            
            // 驗證連續性（無間隙）
            for (i in 0 until dateList.size - 1) {
                val nextDay = dateList[i].plus(1, DateTimeUnit.DAY)
                nextDay shouldBe dateList[i + 1]
            }
        }
    }
    
    "Property 2: 日期範圍生成完整性 - 正確的日期數量" {
        checkAll(100, Arb.localDate(), Arb.localDate()) { date1, date2 ->
            val startDate = minOf(date1, date2)
            val endDate = maxOf(date1, date2)
            val range = startDate..endDate
            
            val dateList = range.toDateList()
            
            // 計算預期的天數
            var expectedDays = 1
            var current = startDate
            while (current < endDate) {
                current = current.plus(1, DateTimeUnit.DAY)
                expectedDays++
            }
            
            dateList shouldHaveSize expectedDays
        }
    }
    
    "Property 2: 單日範圍應該返回單一日期" {
        checkAll(100, Arb.localDate()) { date ->
            val range = date..date
            val dateList = range.toDateList()
            
            dateList shouldHaveSize 1
            dateList.first() shouldBe date
        }
    }
    
    "Property 5: 日期格式化一致性" {
        checkAll(100, Arb.localDate()) { date ->
            val formatted = date.toFriendlyString()
            
            // 驗證格式包含日期和星期
            formatted shouldContain date.toString()
            formatted shouldContain "週"
        }
    }
    
    "Property: 日期範圍檢查正確性" {
        checkAll(100, Arb.localDate(), Arb.localDate(), Arb.localDate()) { date1, date2, testDate ->
            val startDate = minOf(date1, date2)
            val endDate = maxOf(date1, date2)
            val range = startDate..endDate
            
            val isInRange = testDate.isInRange(range)
            val expected = testDate >= startDate && testDate <= endDate
            
            isInRange shouldBe expected
        }
    }
})
