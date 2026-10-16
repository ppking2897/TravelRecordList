@file:OptIn(ExperimentalTime::class)

package io.github.ppking2897.travelrecord.test

import io.github.ppking2897.travelrecord.core.domain.service.ImageStorageService
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Location
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.ItineraryItemRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 合約測試共用的一組 Repository：行程與項目共用同一個資料來源
 */
class TestRepositories(
    val itineraries: ItineraryRepository,
    val items: ItineraryItemRepository,
)

/** 對齊到毫秒的固定時間，不同儲存方式讀回來都能直接比對 */
fun instantAt(epochMillis: Long): Instant = Instant.fromEpochMilliseconds(1_760_000_000_000 + epochMillis)

fun testItinerary(
    id: String,
    title: String = "行程 $id",
    description: String = "",
    createdAtMs: Long = 0,
) = Itinerary(
    id = id,
    title = title,
    description = description,
    startDate = LocalDate(2026, 10, 1),
    endDate = LocalDate(2026, 10, 5),
    createdAt = instantAt(createdAtMs),
    modifiedAt = instantAt(createdAtMs),
)

fun testItem(
    id: String,
    itineraryId: String,
    date: LocalDate = LocalDate(2026, 10, 1),
    arrivalTime: LocalTime? = null,
    departureTime: LocalTime? = null,
    locationName: String = "地點 $id",
    activity: String = "活動 $id",
) = ItineraryItem(
    id = id,
    itineraryId = itineraryId,
    date = date,
    arrivalTime = arrivalTime,
    departureTime = departureTime,
    location = Location(name = locationName),
    activity = activity,
    createdAt = instantAt(0),
    modifiedAt = instantAt(0),
)

/**
 * 不碰檔案系統的圖片儲存：存檔回傳假路徑，讀檔回傳固定內容
 */
class FakeImageStorageService : ImageStorageService {
    private var savedCount = 0

    override suspend fun saveImage(imageData: ByteArray, itemId: String): Result<String> =
        Result.success("fake/${itemId}_${savedCount++}.jpg")

    override suspend fun loadImage(imagePath: String): Result<ByteArray> = Result.success(byteArrayOf(1, 2, 3))

    override suspend fun deleteImage(imagePath: String): Result<Unit> = Result.success(Unit)

    override suspend fun compressImage(imageData: ByteArray, maxSizeKB: Int): Result<ByteArray> =
        Result.success(imageData)

    override suspend fun generateThumbnail(imageData: ByteArray, width: Int, height: Int): Result<ByteArray> =
        Result.success(imageData)

    override fun getImageDirectory(): String = "fake"
}

/** 存圖一律失敗，其餘行為同 [FakeImageStorageService] */
class FailingSaveImageStorageService : ImageStorageService by FakeImageStorageService() {
    override suspend fun saveImage(imageData: ByteArray, itemId: String): Result<String> =
        Result.failure(IllegalStateException("存照片失敗"))
}
