package io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase

import io.github.ppking2897.travelrecord.core.data.local.InMemoryStorageService
import io.github.ppking2897.travelrecord.core.data.local.LegacyJsonImporter
import io.github.ppking2897.travelrecord.core.data.local.db.inMemoryAppDatabase
import io.github.ppking2897.travelrecord.core.data.local.db.repositories
import io.github.ppking2897.travelrecord.core.domain.service.LegacyDataImporter
import io.github.ppking2897.travelrecord.core.data.local.legacy.LegacyJsonItineraryRepository
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * 啟動時先搬舊資料、再決定要不要寫範例資料
 */
class EnsureLocalDataReadyUseCaseTest {

    private val database = inMemoryAppDatabase()
    private val room = database.repositories()
    private val legacyStorage = InMemoryStorageService()

    private fun ensureWith(importer: LegacyDataImporter) =
        EnsureLocalDataReadyUseCase(importer, InitializeSampleDataUseCase(room.itineraries, room.items))

    private val ensure = ensureWith(LegacyJsonImporter(legacyStorage, database))

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
