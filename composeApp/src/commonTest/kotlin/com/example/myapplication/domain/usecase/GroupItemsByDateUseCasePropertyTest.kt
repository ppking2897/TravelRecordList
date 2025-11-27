package com.example.myapplication.domain.usecase

import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.data.model.Location
import com.example.myapplication.data.model.localDate
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Property Test: 項目分組保持順序
 * 
 * Property 4: For any list of items, grouping by date should preserve 
 * the time-based ordering within each date group
 * 
 * Validates: 需求 3.3, 3.4
 */
class GroupItemsByDateUseCasePropertyTest : StringSpec({
    val useCase = GroupItemsByDateUseCase()
    
    "Property 4: 項目分組保持時間順序" {
        checkAll(100, Arb.list(Arb.itineraryItem(), 1..20)) { items ->
            val grouped = useCase(items)
            
            // 驗證每組內的時間順序
            grouped.forEach { group ->
                val times = group.items.map { it.time ?: LocalTime(23, 59, 59) }
                for (i in 0 until times.size - 1) {
                    times[i] shouldBe times[i].coerceAtMost(times[i + 1])
                }
            }
        }
    }
    
    "Property 4: 分組後的日期順序正確" {
        checkAll(100, Arb.list(Arb.itineraryItem(), 1..20)) { items ->
            val grouped = useCase(items)
            
            // 驗證日期順序
            val dates = grouped.map { it.date }
            for (i in 0 until dates.size - 1) {
                dates[i] shouldBe dates[i].coerceAtMost(dates[i + 1])
            }
        }
    }
    
    "Property 4: 分組不丟失項目" {
        checkAll(100, Arb.list(Arb.itineraryItem(), 1..20)) { items ->
            val grouped = useCase(items)
            val totalItems = grouped.sumOf { it.items.size }
            
            totalItems shouldBe items.size
        }
    }
    
    "Property 4: 相同日期的項目在同一組" {
        checkAll(100, Arb.list(Arb.itineraryItem(), 1..20)) { items ->
            val grouped = useCase(items)
            
            grouped.forEach { group ->
                group.items.forEach { item ->
                    item.date shouldBe group.date
                }
            }
        }
    }
})

/**
 * 生成隨機 ItineraryItem 的 Arb
 */
fun Arb.Companion.itineraryItem(): Arb<ItineraryItem> = arbitrary {
    ItineraryItem(
        id = Arb.string(10..20).bind(),
        itineraryId = Arb.string(10..20).bind(),
        location = Location(
            name = Arb.string(5..20).bind(),
            latitude = Arb.double(-90.0..90.0).bind(),
            longitude = Arb.double(-180.0..180.0).bind()
        ),
        activity = Arb.string(10..50).bind(),
        date = Arb.localDate().bind(),
        time = Arb.localTime().orNull().bind(),
        notes = Arb.string(0..100).bind(),
        photoUrls = emptyList(),
        isCompleted = Arb.boolean().bind(),
        completedAt = null,
        createdAt = Instant.fromEpochMilliseconds(Arb.long(0..System.currentTimeMillis()).bind()),
        modifiedAt = Instant.fromEpochMilliseconds(Arb.long(0..System.currentTimeMillis()).bind())
    )
}

/**
 * 生成隨機 LocalTime 的 Arb
 */
fun Arb.Companion.localTime(): Arb<LocalTime> = arbitrary {
    val hour = Arb.int(0..23).bind()
    val minute = Arb.int(0..59).bind()
    LocalTime(hour, minute)
}
