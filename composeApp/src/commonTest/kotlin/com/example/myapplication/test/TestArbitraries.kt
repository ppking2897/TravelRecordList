package com.example.myapplication.test

import com.example.myapplication.domain.entity.Hashtag
import com.example.myapplication.domain.entity.ItineraryItem
import com.example.myapplication.domain.entity.Location
import com.example.myapplication.domain.entity.Photo
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * 測試用的 Arb (Arbitrary) 生成器
 */

/**
 * 生成隨機 LocalDate 的 Arb
 */
fun Arb.Companion.localDate(): Arb<LocalDate> = arbitrary {
    val year = Arb.int(2020..2030).bind()
    val month = Arb.int(1..12).bind()
    val day = Arb.int(1..28).bind() // 使用 28 避免月份邊界問題
    LocalDate(year, month, day)
}

/**
 * 生成隨機 LocalTime 的 Arb
 */
fun Arb.Companion.localTime(): Arb<LocalTime> = arbitrary {
    val hour = Arb.int(0..23).bind()
    val minute = Arb.int(0..59).bind()
    LocalTime(hour, minute)
}

/**
 * 生成隨機 Location 的 Arb
 */
fun Arb.Companion.location(): Arb<Location> = arbitrary {
    Location(
        name = Arb.string(5..20).bind(),
        address = Arb.string(10..50).bind(),
        latitude = Arb.double(-90.0..90.0).bind(),
        longitude = Arb.double(-180.0..180.0).bind()
    )
}

/**
 * 生成隨機 ItineraryItem 的 Arb
 */
fun Arb.Companion.itineraryItem(): Arb<ItineraryItem> = arbitrary {
    val now = Clock.System.now()
    ItineraryItem(
        id = Arb.uuid().bind().toString(),
        itineraryId = Arb.uuid().bind().toString(),
        date = Arb.localDate().bind(),
        arrivalTime = Arb.localTime().orNull().bind(),
        departureTime = Arb.localTime().orNull().bind(),
        location = Arb.location().bind(),
        activity = Arb.string(5..30).bind(),
        notes = Arb.string(0..100).bind(),
        hashtags = emptyList(),
        photos = emptyList(),
        coverPhotoId = null,
        isCompleted = Arb.boolean().bind(),
        completedAt = null,
        createdAt = now,
        modifiedAt = now
    )
}
