@file:OptIn(ExperimentalTime::class)

package io.github.ppking2897.travelrecord.core.data.local

import io.github.ppking2897.travelrecord.core.data.local.db.inMemoryAppDatabase
import io.github.ppking2897.travelrecord.core.data.local.db.repositories
import io.github.ppking2897.travelrecord.core.domain.sample.SampleDataProvider
import io.github.ppking2897.travelrecord.feature.itinerary.data.mapper.toDto
import io.github.ppking2897.travelrecord.core.data.local.legacy.LegacyJsonItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.DeleteItineraryUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.InitializeSampleDataUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.HashtagRepositoryImpl
import io.github.ppking2897.travelrecord.core.data.local.legacy.LegacyJsonItineraryItemRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Location
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.AddItineraryItemUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.BatchDeleteItemsUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.BatchUpdateItemsUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.ExtractHashtagsUseCase
import io.github.ppking2897.travelrecord.test.FakeImageStorageService
import io.github.ppking2897.travelrecord.test.instantAt
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 舊版鍵值資料搬進 Room
 *
 * 舊資料由舊版的 Repository 與 use case 實際寫出來，不手刻 JSON；
 * 另有一份凍結的 JSON 字串，確保舊版程式碼移除後仍能驗證舊格式。
 */
class LegacyJsonImporterTest {

    /** 舊版 App：JSON 鍵值儲存 + 原本的 use case */
    private class LegacyApp(val storage: StorageService = InMemoryStorageService()) {
        val itineraries = LegacyJsonItineraryRepository(storage)
        val items = LegacyJsonItineraryItemRepository(storage)
        val addItem = AddItineraryItemUseCase(items, itineraries, ExtractHashtagsUseCase(HashtagRepositoryImpl()))

        suspend fun seedSamples() = InitializeSampleDataUseCase(itineraries, items)().getOrThrow()

        suspend fun createTrip(id: String, createdAtMs: Long, vararg locationNames: String): List<ItineraryItem> {
            itineraries.createItinerary(testItinerary(id = id, createdAtMs = createdAtMs)).getOrThrow()
            return locationNames.map { name ->
                addItem(
                    itineraryId = id,
                    date = LocalDate(2026, 10, 1),
                    location = Location(name = name),
                    activity = "參觀 $name",
                    notes = "",
                    currentTimestamp = instantAt(0),
                ).getOrThrow()
            }
        }
    }

    private val database = inMemoryAppDatabase()
    private val room = database.repositories()

    private suspend fun importFrom(legacy: LegacyApp) = LegacyJsonImporter(legacy.storage, database).importIfNeeded()

    private suspend fun StorageService.snapshot(): Map<String, String?> =
        getAllKeys().getOrThrow().associateWith { load(it).getOrThrow() }

    @Test
    fun `舊資料是空的：不搬任何東西，但記下已搬過`() = runTest {
        val legacy = LegacyApp()

        importFrom(legacy).getOrThrow()

        room.itineraries.getAllItineraries().getOrThrow() shouldBe emptyList()
        legacy.storage.load(LegacyJsonImporter.MARKER_KEY).getOrThrow() shouldNotBe null
    }

    @Test
    fun `範例資料只存在單獨鍵的項目也會搬過去，讀行程時帶得出來`() = runTest {
        val legacy = LegacyApp().apply { seedSamples() }

        importFrom(legacy).getOrThrow()

        val sampleId = SampleDataProvider.getSampleItinerary().id
        room.itineraries.getAllItineraries().getOrThrow().size shouldBe 2
        room.itineraries.getItinerary(sampleId).getOrThrow()!!.items.size shouldBe 8
        // 時間點搬過去會截到毫秒（另由凍結格式的測試驗證），這裡只比對項目與順序
        room.items.getItemsByItinerary(sampleId).getOrThrow().map { it.id } shouldBe
            legacy.items.getItemsByItinerary(sampleId).getOrThrow().map { it.id }
    }

    @Test
    fun `批次刪除過的項目不會被救回來`() = runTest {
        val legacy = LegacyApp()
        val (deleted, kept) = legacy.createTrip("trip", 0, "嵐山竹林", "奈良公園")
        BatchDeleteItemsUseCase(legacy.items, FakeImageStorageService())(listOf(deleted.id)).getOrThrow()

        importFrom(legacy).getOrThrow()

        room.items.getItemsByItinerary("trip").getOrThrow().map { it.id } shouldBe listOf(kept.id)
    }

    @Test
    fun `兩份內容不同時以單獨存的那份為準`() = runTest {
        val legacy = LegacyApp()
        val (item) = legacy.createTrip("trip", 0, "金閣寺")
        BatchUpdateItemsUseCase(legacy.items).markComplete(listOf(item.id)).getOrThrow()

        importFrom(legacy).getOrThrow()

        room.items.getItem(item.id).getOrThrow()!!.isCompleted shouldBe true
    }

    @Test
    fun `找不到所屬行程的項目不搬`() = runTest {
        val legacy = LegacyApp().apply { seedSamples() }
        val sampleId = SampleDataProvider.getSampleItinerary().id
        DeleteItineraryUseCase(legacy.itineraries, legacy.items, FakeImageStorageService())(sampleId).getOrThrow()

        importFrom(legacy).getOrThrow()

        room.itineraries.getAllItineraries().getOrThrow().map { it.id } shouldBe
            listOf(SampleDataProvider.getSamplePastItinerary().id)
        room.items.getItem("sample-item-1").getOrThrow() shouldBe null
    }

    @Test
    fun `保留原本的新增順序：索引有但資料不存在的跳過，不在索引裡的排在後面`() = runTest {
        val legacy = LegacyApp()
        legacy.createTrip("a", 0)
        legacy.createTrip("b", 0)
        legacy.createTrip("c", 0)
        legacy.storage.delete("itinerary:b").getOrThrow()
        legacy.storage.save("itinerary:z", JsonSerializer.serializeItinerary(testItinerary(id = "z").toDto()))
            .getOrThrow()

        importFrom(legacy).getOrThrow()

        room.itineraries.getAllItineraries().getOrThrow().map { it.id } shouldBe listOf("a", "c", "z")
    }

    @Test
    fun `同一天沒有時間的項目維持原本的新增順序`() = runTest {
        val legacy = LegacyApp()
        val (first, second) = legacy.createTrip("trip", 0, "第一站", "第二站")

        importFrom(legacy).getOrThrow()

        room.items.getItemsByItinerary("trip").getOrThrow().map { it.id } shouldBe listOf(first.id, second.id)
    }

    @Test
    fun `損壞的單筆資料跳過，其餘照常搬`() = runTest {
        val legacy = LegacyApp()
        val (good) = legacy.createTrip("trip", 0, "清水寺")
        legacy.storage.save("item:broken", "{ not json").getOrThrow()

        importFrom(legacy).getOrThrow()

        room.items.getItemsByItinerary("trip").getOrThrow().map { it.id } shouldBe listOf(good.id)
    }

    @Test
    fun `讀取舊資料失敗：回傳失敗、不記已搬過、資料庫維持空的`() = runTest {
        val storage = FailingLoadStorage(failKey = "item:index")
        val legacy = LegacyApp(storage).apply { seedSamples() }

        importFrom(legacy).isFailure shouldBe true

        storage.load(LegacyJsonImporter.MARKER_KEY).getOrThrow() shouldBe null
        room.itineraries.getAllItineraries().getOrThrow() shouldBe emptyList()
    }

    @Test
    fun `資料庫已有資料就不搬，只補記已搬過`() = runTest {
        room.itineraries.createItinerary(testItinerary(id = "already-in-room")).getOrThrow()
        val legacy = LegacyApp().apply { seedSamples() }

        importFrom(legacy).getOrThrow()

        room.itineraries.getAllItineraries().getOrThrow().map { it.id } shouldBe listOf("already-in-room")
        legacy.storage.load(LegacyJsonImporter.MARKER_KEY).getOrThrow() shouldNotBe null
    }

    @Test
    fun `已經搬過就不再搬，即使之後使用者把行程刪光`() = runTest {
        val legacy = LegacyApp().apply { seedSamples() }
        importFrom(legacy).getOrThrow()
        room.itineraries.getAllItineraries().getOrThrow().forEach {
            room.itineraries.deleteItinerary(it.id).getOrThrow()
        }

        importFrom(legacy).getOrThrow()

        room.itineraries.getAllItineraries().getOrThrow() shouldBe emptyList()
    }

    @Test
    fun `搬完後舊資料原封不動`() = runTest {
        val legacy = LegacyApp().apply { seedSamples() }
        val before = legacy.storage.snapshot()

        importFrom(legacy).getOrThrow()

        legacy.storage.snapshot() - LegacyJsonImporter.MARKER_KEY shouldBe before
    }

    @Test
    fun `凍結的舊格式：時間點截到毫秒，日期時間精確，照片路徑與照片時間原樣保留`() = runTest {
        val legacy = LegacyApp()
        legacy.storage.save("itinerary:frozen-trip", FROZEN_ITINERARY_JSON).getOrThrow()
        legacy.storage.save("itinerary:index", """["frozen-trip"]""").getOrThrow()
        legacy.storage.save("item:frozen-item", FROZEN_ITEM_JSON).getOrThrow()
        legacy.storage.save("item:index", """["frozen-item"]""").getOrThrow()

        importFrom(legacy).getOrThrow()

        val itinerary = room.itineraries.getItinerary("frozen-trip").getOrThrow()!!
        itinerary.createdAt shouldBe Instant.parse("2026-10-01T08:00:00.123Z")
        itinerary.coverPhotoPath shouldBe "/data/user/0/io.github.ppking2897.travelrecord/files/itinerary_images/cover.jpg"
        val item = itinerary.items.single()
        item.date shouldBe LocalDate(2026, 10, 2)
        item.arrivalTime shouldBe LocalTime(9, 30)
        item.departureTime shouldBe LocalTime(11, 45, 30)
        item.completedAt shouldBe Instant.parse("2026-10-02T03:00:00.555Z")
        item.photos.single().filePath shouldBe
            "/data/user/0/io.github.ppking2897.travelrecord/files/itinerary_images/frozen-item_1.jpg"
        item.photos.single().uploadedAt shouldBe Instant.parse("2026-10-02T01:00:00.111222333Z")
        item.hashtags.single().tag shouldBe "京都"
        item.coverPhotoId shouldBe "p1"
        item.isCompleted shouldBe true
    }

    private class FailingLoadStorage(
        private val failKey: String,
        private val delegate: StorageService = InMemoryStorageService(),
    ) : StorageService by delegate {
        var failing = false

        override suspend fun load(key: String): Result<String?> =
            if (failing && key == failKey) Result.failure(IllegalStateException("讀取失敗：$key")) else delegate.load(key)

        override suspend fun getAllKeys(): Result<List<String>> {
            failing = true
            return delegate.getAllKeys()
        }
    }

    private companion object {
        const val FROZEN_ITINERARY_JSON = """
            {
                "id": "frozen-trip",
                "title": "凍結行程",
                "startDate": "2026-10-01",
                "endDate": "2026-10-03",
                "coverPhotoPath": "/data/user/0/io.github.ppking2897.travelrecord/files/itinerary_images/cover.jpg",
                "items": [],
                "createdAt": "2026-10-01T08:00:00.123456789Z",
                "modifiedAt": "2026-10-01T08:00:00.123456789Z"
            }
        """

        const val FROZEN_ITEM_JSON = """
            {
                "id": "frozen-item",
                "itineraryId": "frozen-trip",
                "date": "2026-10-02",
                "arrivalTime": "09:30",
                "departureTime": "11:45:30",
                "location": { "name": "清水寺", "latitude": 34.9949, "longitude": 135.785, "address": "京都" },
                "activity": "賞景",
                "notes": "#京都",
                "hashtags": [
                    { "tag": "京都", "usageCount": 1, "firstUsed": "2026-10-02T01:00:00.987654321Z", "lastUsed": "2026-10-02T01:00:00.987654321Z" }
                ],
                "photos": [
                    {
                        "id": "p1",
                        "itemId": "frozen-item",
                        "fileName": "frozen-item_1.jpg",
                        "filePath": "/data/user/0/io.github.ppking2897.travelrecord/files/itinerary_images/frozen-item_1.jpg",
                        "order": 0,
                        "fileSize": 2048,
                        "uploadedAt": "2026-10-02T01:00:00.111222333Z",
                        "modifiedAt": "2026-10-02T01:00:00.111222333Z"
                    }
                ],
                "coverPhotoId": "p1",
                "isCompleted": true,
                "completedAt": "2026-10-02T03:00:00.555666777Z",
                "createdAt": "2026-10-02T00:00:00.999999999Z",
                "modifiedAt": "2026-10-02T00:00:00.999999999Z"
            }
        """
    }
}
