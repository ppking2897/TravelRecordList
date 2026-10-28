package io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase

import io.github.ppking2897.travelrecord.core.data.local.InMemoryStorageService
import io.github.ppking2897.travelrecord.core.data.local.LegacyJsonImporter
import io.github.ppking2897.travelrecord.core.data.local.db.inMemoryAppDatabase
import io.github.ppking2897.travelrecord.core.data.local.db.repositories
import io.github.ppking2897.travelrecord.core.domain.service.LegacyDataImporter
import io.github.ppking2897.travelrecord.core.data.local.legacy.LegacyJsonItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary.data.repository.SampleDataRecordRepositoryImpl
import io.github.ppking2897.travelrecord.feature.route.data.repository.RouteRepositoryImpl
import io.github.ppking2897.travelrecord.feature.route.domain.repository.RouteRepository
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * 啟動時先清掉舊版存下的路線、搬舊資料，再決定要不要寫範例資料
 */
class EnsureLocalDataReadyUseCaseTest {

    private val database = inMemoryAppDatabase()
    private val room = database.repositories()
    private val legacyStorage = InMemoryStorageService()

    /** 跟 App 一樣，路線與舊資料用同一個鍵值儲存 */
    private val routes = RouteRepositoryImpl(legacyStorage, room.itineraries)

    /** 「範例已放過」另外存，才不會混進上面檢查鍵值儲存內容的斷言 */
    private val sampleDataRecord = SampleDataRecordRepositoryImpl(InMemoryStorageService())

    private fun ensureWith(
        importer: LegacyDataImporter = LegacyJsonImporter(legacyStorage, database),
        routeRepository: RouteRepository = routes,
    ) = EnsureLocalDataReadyUseCase(importer, InitializeSampleDataUseCase(room.itineraries, room.items, sampleDataRecord), routeRepository)

    private val ensure = ensureWith()

    @Test
    fun `已搬過資料的手機，啟動時也清掉舊版存的路線`() = runTest {
        legacyStorage.save(LegacyJsonImporter.MARKER_KEY, "1").getOrThrow()
        legacyStorage.save("route:a1", "{}").getOrThrow()
        legacyStorage.save("route:index", """["a1"]""").getOrThrow()

        ensure().getOrThrow()

        legacyStorage.getAllKeys().getOrThrow() shouldContainExactlyInAnyOrder listOf(LegacyJsonImporter.MARKER_KEY)
    }

    @Test
    fun `清路線失敗時照常啟動`() = runTest {
        val failingRoutes = object : RouteRepository by routes {
            override suspend fun deleteStoredRoutes() = Result.failure<Unit>(IllegalStateException("刪不掉"))
        }

        ensureWith(routeRepository = failingRoutes)().isSuccess shouldBe true

        room.itineraries.getAllItineraries().getOrThrow().size shouldBe 2
    }

    @Test
    fun `有舊資料時搬過來，不寫範例資料`() = runTest {
        LegacyJsonItineraryRepository(legacyStorage).createItinerary(testItinerary(id = "my-trip")).getOrThrow()

        ensure().getOrThrow()

        room.itineraries.getAllItineraries().getOrThrow().map { it.id } shouldBe listOf("my-trip")
    }

    @Test
    fun `沒有舊資料時寫入範例資料，重跑也只有一份`() = runTest {
        ensure().getOrThrow()
        ensure().getOrThrow()

        room.itineraries.getAllItineraries().getOrThrow().size shouldBe 2
    }

    @Test
    fun `搬移失敗時回傳失敗，也不寫範例資料`() = runTest {
        val failing = object : LegacyDataImporter {
            override suspend fun importIfNeeded() = Result.failure<Unit>(IllegalStateException("讀不到舊資料"))
        }

        ensureWith(failing)().isFailure shouldBe true

        room.itineraries.getAllItineraries().getOrThrow() shouldBe emptyList()
    }
}
