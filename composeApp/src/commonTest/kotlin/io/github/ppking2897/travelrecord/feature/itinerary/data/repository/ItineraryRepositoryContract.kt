@file:OptIn(ExperimentalTime::class)

package io.github.ppking2897.travelrecord.feature.itinerary.data.repository

import io.github.ppking2897.travelrecord.test.TestRepositories
import io.github.ppking2897.travelrecord.test.instantAt
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.time.ExperimentalTime

/**
 * ItineraryRepository 的行為合約，不論底層用哪種儲存方式都必須成立
 *
 * 子類別提供 Repository 實例；每個測試都拿到一份全新的空資料。
 * 只比對行程本身的欄位，行程內含的項目由 DataLayerAcceptanceContract 驗證。
 */
abstract class ItineraryRepositoryContract {

    protected abstract fun newRepositories(): TestRepositories

    @Test
    fun `新增後讀出的行程與寫入的相同`() = runTest {
        val repo = newRepositories().itineraries
        val itinerary = testItinerary(id = "it-1", title = "京都", description = "賞楓")

        repo.createItinerary(itinerary).getOrThrow()

        repo.getItinerary("it-1").getOrThrow() shouldBe itinerary
    }

    @Test
    fun `讀取不存在的行程回傳 null`() = runTest {
        val repo = newRepositories().itineraries

        repo.getItinerary("missing").getOrThrow() shouldBe null
    }

    @Test
    fun `全部行程依建立時間由新到舊，時間相同時保留新增順序`() = runTest {
        val repo = newRepositories().itineraries
        repo.createItinerary(testItinerary(id = "a", createdAtMs = 1_000)).getOrThrow()
        repo.createItinerary(testItinerary(id = "b", createdAtMs = 3_000)).getOrThrow()
        repo.createItinerary(testItinerary(id = "c", createdAtMs = 2_000)).getOrThrow()
        repo.createItinerary(testItinerary(id = "d", createdAtMs = 3_000)).getOrThrow()

        repo.getAllItineraries().getOrThrow().map { it.id } shouldBe listOf("b", "d", "c", "a")
    }

    @Test
    fun `更新會保存行程的欄位`() = runTest {
        val repo = newRepositories().itineraries
        val original = testItinerary(id = "it-1")
        repo.createItinerary(original).getOrThrow()
        val updated = original.copy(
            title = "新標題",
            description = "新描述",
            startDate = LocalDate(2026, 11, 1),
            endDate = null,
            coverPhotoPath = "/data/cover.jpg",
            modifiedAt = instantAt(9_000),
        )

        repo.updateItinerary(updated).getOrThrow()

        repo.getItinerary("it-1").getOrThrow() shouldBe updated
    }

    @Test
    fun `更新不存在的行程回傳失敗`() = runTest {
        val repo = newRepositories().itineraries

        repo.updateItinerary(testItinerary(id = "missing")).isFailure shouldBe true
    }

    @Test
    fun `刪除後讀不到，也不在全部行程中`() = runTest {
        val repo = newRepositories().itineraries
        repo.createItinerary(testItinerary(id = "keep")).getOrThrow()
        repo.createItinerary(testItinerary(id = "gone")).getOrThrow()

        repo.deleteItinerary("gone").getOrThrow()

        repo.getItinerary("gone").getOrThrow() shouldBe null
        repo.getAllItineraries().getOrThrow().map { it.id } shouldBe listOf("keep")
    }

    @Test
    fun `刪除不存在的行程視為成功`() = runTest {
        val repo = newRepositories().itineraries

        repo.deleteItinerary("missing").isSuccess shouldBe true
    }

    @Test
    fun `空白搜尋回傳全部行程，順序與全部行程相同`() = runTest {
        val repo = newRepositories().itineraries
        repo.createItinerary(testItinerary(id = "a", createdAtMs = 1_000)).getOrThrow()
        repo.createItinerary(testItinerary(id = "b", createdAtMs = 2_000)).getOrThrow()

        repo.searchItineraries("  ").getOrThrow().map { it.id } shouldBe listOf("b", "a")
    }

    @Test
    fun `搜尋比對標題與描述，不分大小寫`() = runTest {
        val repo = newRepositories().itineraries
        repo.createItinerary(testItinerary(id = "by-title", title = "Kyoto Trip", createdAtMs = 1_000)).getOrThrow()
        repo.createItinerary(
            testItinerary(id = "by-desc", title = "關西", description = "途經 KYOTO 車站", createdAtMs = 2_000)
        ).getOrThrow()
        repo.createItinerary(testItinerary(id = "other", title = "東京", createdAtMs = 3_000)).getOrThrow()

        repo.searchItineraries("kyoto").getOrThrow().map { it.id } shouldBe listOf("by-desc", "by-title")
        repo.searchItineraries("osaka").getOrThrow() shouldBe emptyList()
    }
}
