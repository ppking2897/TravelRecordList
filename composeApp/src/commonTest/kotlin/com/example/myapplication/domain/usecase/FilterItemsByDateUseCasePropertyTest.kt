package com.example.myapplication.domain.usecase

import com.example.myapplication.data.model.localDate
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import kotlinx.datetime.LocalDate

/**
 * Property Test: 日期篩選正確性
 * 
 * Property 3: For any list of items and selected date, 
 * filtering by that date should return only items with matching date
 * 
 * Validates: 需求 2.3, 6.1
 */
class FilterItemsByDateUseCasePropertyTest : StringSpec({
    val useCase = FilterItemsByDateUseCase()
    
    "Property 3: 篩選後的項目都匹配選中的日期" {
        checkAll(100, Arb.list(Arb.itineraryItem(), 1..20), Arb.localDate()) { items, selectedDate ->
            val filtered = useCase(items, selectedDate)
            
            // 所有篩選後的項目都應該匹配選中的日期
            filtered.forEach { item ->
                item.date shouldBe selectedDate
            }
        }
    }
    
    "Property 3: null 日期返回所有項目" {
        checkAll(100, Arb.list(Arb.itineraryItem(), 1..20)) { items ->
            val filtered = useCase(items, null)
            
            filtered shouldHaveSize items.size
            filtered shouldBe items
        }
    }
    
    "Property 3: 篩選不存在的日期返回空列表" {
        checkAll(100, Arb.list(Arb.itineraryItem(), 1..20), Arb.localDate()) { items, selectedDate ->
            // 確保沒有項目匹配這個日期
            val itemsWithoutDate = items.filter { it.date != selectedDate }
            val filtered = useCase(itemsWithoutDate, selectedDate)
            
            filtered.shouldBeEmpty()
        }
    }
    
    "Property 3: 篩選結果數量正確" {
        checkAll(100, Arb.list(Arb.itineraryItem(), 1..20), Arb.localDate()) { items, selectedDate ->
            val filtered = useCase(items, selectedDate)
            val expected = items.count { it.date == selectedDate }
            
            filtered shouldHaveSize expected
        }
    }
})
