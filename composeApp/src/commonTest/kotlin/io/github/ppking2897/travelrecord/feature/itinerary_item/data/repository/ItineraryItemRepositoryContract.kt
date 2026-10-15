@file:OptIn(ExperimentalTime::class)

package io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository

import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Hashtag
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Location
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Photo
import io.github.ppking2897.travelrecord.test.TestRepositories
import io.github.ppking2897.travelrecord.test.instantAt
import io.github.ppking2897.travelrecord.test.testItem
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.time.ExperimentalTime

/**
 * ItineraryItemRepository 的行為合約，不論底層用哪種儲存方式都必須成立
 *
 * 項目一律先建立所屬行程再新增，資料庫實作會要求行程存在。
 */
abstract class ItineraryItemRepositoryContract {

    protected abstract fun newRepositories(): TestRepositories

    private suspend fun TestRepositories.withItinerary(id: String): TestRepositories {
        itineraries.createItinerary(testItinerary(id = id)).getOrThrow()
        return this
    }

    private fun photo(id: String, itemId: String, order: Int) = Photo(
        id = id,
        itemId = itemId,
        fileName = "$id.jpg",
        filePath = "/data/user/0/app/files/itinerary_images/$id.jpg",
        thumbnailPath = "/data/user/0/app/files/itinerary_images/${id}_thumb.jpg",
        order = order,
        width = 1080,
        height = 720,
        fileSize = 2048,
        uploadedAt = instantAt(100L + order),
        modifiedAt = instantAt(200L + order),
    )

    private fun fullItem(): ItineraryItem = ItineraryItem(
        id = "item-1",
        itineraryId = "it-1",
        date = LocalDate(2026, 10, 2),
        arrivalTime = LocalTime(9, 30),
        departureTime = LocalTime(11, 45),
        location = Location(name = "清水寺", latitude = 34.9949, longitude = 135.7850, address = "京都府京都市東山区"),
        activity = "清水舞台賞景",
        notes = "記得拍照 #京都 #寺廟",
        hashtags = listOf(
            Hashtag(tag = "京都", usageCount = 1, firstUsed = instantAt(10), lastUsed = instantAt(10)),
            Hashtag(tag = "寺廟", usageCount = 1, firstUsed = instantAt(10), lastUsed = instantAt(10)),
        ),
        photos = listOf(photo("p-b", "item-1", order = 0), photo("p-a", "item-1", order = 1)),
        coverPhotoId = "p-a",
        isCompleted = true,
        completedAt = instantAt(5_000),
        order = 3,
        createdAt = instantAt(1_000),
        modifiedAt = instantAt(2_000),
    )

    @Test
    fun `新增後讀出的項目與寫入的相同`() = runTest {
        val repos = newRepositories().withItinerary("it-1")
        val item = fullItem()

        repos.items.addItem(item).getOrThrow()

        repos.items.getItem("item-1").getOrThrow() shouldBe item
    }

    @Test
    fun `讀取不存在的項目回傳 null`() = runTest {
        val repos = newRepositories()

        repos.items.getItem("missing").getOrThrow() shouldBe null
    }

    @Test
    fun `更新會保存變更，包含整份照片清單`() = runTest {
        val repos = newRepositories().withItinerary("it-1")
        val original = fullItem()
        repos.items.addItem(original).getOrThrow()
        val updated = original.copy(
            activity = "改去二年坂",
            arrivalTime = null,
            isCompleted = false,
            completedAt = null,
            photos = listOf(photo("p-a", "item-1", order = 0)),
            coverPhotoId = null,
            modifiedAt = instantAt(9_000),
        )

        repos.items.updateItem(updated).getOrThrow()

        repos.items.getItem("item-1").getOrThrow() shouldBe updated
    }

    @Test
    fun `更新不存在的項目回傳失敗`() = runTest {
        val repos = newRepositories().withItinerary("it-1")

        repos.items.updateItem(testItem(id = "missing", itineraryId = "it-1")).isFailure shouldBe true
    }

    @Test
    fun `刪除後讀不到，也不在所屬行程的項目中`() = runTest {
        val repos = newRepositories().withItinerary("it-1")
        repos.items.addItem(testItem(id = "keep", itineraryId = "it-1")).getOrThrow()
        repos.items.addItem(testItem(id = "gone", itineraryId = "it-1")).getOrThrow()

        repos.items.deleteItem("gone").getOrThrow()

        repos.items.getItem("gone").getOrThrow() shouldBe null
        repos.items.getItemsByItinerary("it-1").getOrThrow().map { it.id } shouldBe listOf("keep")
    }

    @Test
    fun `刪除不存在的項目視為成功`() = runTest {
        val repos = newRepositories()

        repos.items.deleteItem("missing").isSuccess shouldBe true
    }

    @Test
    fun `只回傳指定行程的項目`() = runTest {
        val repos = newRepositories().withItinerary("it-1").withItinerary("it-2")
        repos.items.addItem(testItem(id = "a", itineraryId = "it-1")).getOrThrow()
        repos.items.addItem(testItem(id = "b", itineraryId = "it-2")).getOrThrow()
        repos.items.addItem(testItem(id = "c", itineraryId = "it-1")).getOrThrow()

        repos.items.getItemsByItinerary("it-1").getOrThrow().map { it.id } shouldBe listOf("a", "c")
        repos.items.getItemsByItinerary("none").getOrThrow() shouldBe emptyList()
    }

    @Test
    fun `項目依日期再依抵達或離開時間排序，沒有時間的排前面，相同時保留新增順序`() = runTest {
        val repos = newRepositories().withItinerary("it-1")
        val day1 = LocalDate(2026, 10, 1)
        val day2 = LocalDate(2026, 10, 2)
        listOf(
            testItem(id = "d2-10:00-first", itineraryId = "it-1", date = day2, arrivalTime = LocalTime(10, 0)),
            testItem(id = "d1-depart-15:00", itineraryId = "it-1", date = day1, departureTime = LocalTime(15, 0)),
            testItem(id = "d1-no-time-first", itineraryId = "it-1", date = day1),
            testItem(id = "d1-09:00", itineraryId = "it-1", date = day1, arrivalTime = LocalTime(9, 0)),
            testItem(id = "d1-no-time-second", itineraryId = "it-1", date = day1),
            testItem(id = "d2-10:00-second", itineraryId = "it-1", date = day2, arrivalTime = LocalTime(10, 0)),
        ).forEach { repos.items.addItem(it).getOrThrow() }

        repos.items.getItemsByItinerary("it-1").getOrThrow().map { it.id } shouldBe listOf(
            "d1-no-time-first",
            "d1-no-time-second",
            "d1-09:00",
            "d1-depart-15:00",
            "d2-10:00-first",
            "d2-10:00-second",
        )
    }
}
