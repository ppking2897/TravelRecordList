@file:OptIn(ExperimentalTime::class)

package io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase

import io.github.ppking2897.travelrecord.core.domain.service.ImageStorageService
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.ItineraryItemRepository
import io.github.ppking2897.travelrecord.test.FailingDeleteImageStorageService
import io.github.ppking2897.travelrecord.test.FailingDeleteItemRepository
import io.github.ppking2897.travelrecord.test.FailingDeleteItineraryRepository
import io.github.ppking2897.travelrecord.test.InMemoryItineraryItemRepository
import io.github.ppking2897.travelrecord.test.InMemoryItineraryRepository
import io.github.ppking2897.travelrecord.test.RecordingImageStorageService
import io.github.ppking2897.travelrecord.test.TestUseCases
import io.github.ppking2897.travelrecord.test.testItem
import io.github.ppking2897.travelrecord.test.testItinerary
import io.github.ppking2897.travelrecord.test.testPhoto
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

/**
 * 刪除項目或行程時，照片與封面檔要跟著刪，測試名稱對應 A 組修正討論時定下的情境
 */
class PhotoFileCleanupTest {

    private val kyotoCover = "covers/kyoto.jpg"
    private val kyoto = testItinerary("kyoto").copy(coverPhotoPath = kyotoCover)
    private val tokyo = testItinerary("tokyo")

    /** 京都的 a 有兩張照片（第一張有縮圖）、b 有一張；東京的 c 有一張 */
    private val itemA = testItem("a", "kyoto").copy(
        photos = listOf(testPhoto("pa1", "a").copy(thumbnailPath = "fake/pa1_thumb.jpg"), testPhoto("pa2", "a", order = 1))
    )
    private val itemB = testItem("b", "kyoto").copy(photos = listOf(testPhoto("pb", "b")))
    private val itemC = testItem("c", "tokyo").copy(photos = listOf(testPhoto("pc", "c")))

    private val imageStorage = RecordingImageStorageService()

    @Test
    fun `刪一個項目`() = runTest {
        val useCases = useCasesOf()

        useCases.deleteItem("a").getOrThrow()

        imageStorage.deleted shouldContainExactlyInAnyOrder listOf("fake/pa1.jpg", "fake/pa1_thumb.jpg", "fake/pa2.jpg")
    }

    @Test
    fun `批次刪除`() = runTest {
        val useCases = useCasesOf()

        useCases.batchDeleteItems(listOf("a", "b")).getOrThrow()

        imageStorage.deleted shouldContainExactlyInAnyOrder
            listOf("fake/pa1.jpg", "fake/pa1_thumb.jpg", "fake/pa2.jpg", "fake/pb.jpg")
    }

    @Test
    fun `刪行程`() = runTest {
        val useCases = useCasesOf()

        useCases.deleteItinerary("kyoto").getOrThrow()

        imageStorage.deleted shouldContainExactlyInAnyOrder
            listOf("fake/pa1.jpg", "fake/pa1_thumb.jpg", "fake/pa2.jpg", "fake/pb.jpg", kyotoCover)
    }

    @Test
    fun `照片檔本來就不在`() = runTest {
        val useCases = useCasesOf(storage = FailingDeleteImageStorageService())

        useCases.deleteItem("a").isSuccess.shouldBeTrue()
        useCases.batchDeleteItems(listOf("c")).isSuccess.shouldBeTrue()
        useCases.deleteItinerary("kyoto").isSuccess.shouldBeTrue()
    }

    @Test
    fun `資料庫刪除失敗`() = runTest {
        val failingItems = useCasesOf(wrapItems = ::FailingDeleteItemRepository)
        failingItems.deleteItem("a").isSuccess.shouldBeFalse()
        failingItems.batchDeleteItems(listOf("a", "b")).isSuccess.shouldBeFalse()

        val failingItinerary = useCasesOf(wrapItineraries = ::FailingDeleteItineraryRepository)
        failingItinerary.deleteItinerary("kyoto").isSuccess.shouldBeFalse()

        imageStorage.deleted.shouldBeEmpty()
    }

    // ── 測試工具 ──

    private fun useCasesOf(
        storage: ImageStorageService = imageStorage,
        wrapItems: (ItineraryItemRepository) -> ItineraryItemRepository = { it },
        wrapItineraries: (ItineraryRepository) -> ItineraryRepository = { it },
    ): TestUseCases {
        val itemRepository = wrapItems(InMemoryItineraryItemRepository(itemA, itemB, itemC))
        val itineraryRepository = wrapItineraries(InMemoryItineraryRepository(itemRepository, kyoto, tokyo))
        return TestUseCases(itineraryRepository, itemRepository, imageStorage = storage)
    }
}
