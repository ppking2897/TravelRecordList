package io.github.ppking2897.travelrecord.core.data.local

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import io.github.ppking2897.travelrecord.core.data.local.db.AppDatabase
import io.github.ppking2897.travelrecord.core.domain.service.LegacyDataImporter
import io.github.ppking2897.travelrecord.feature.itinerary.data.local.toEntity
import io.github.ppking2897.travelrecord.feature.itinerary.data.mapper.toDomain
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.local.toEntity
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.mapper.toDomain

/**
 * 把舊版鍵值儲存（每筆一個 JSON 字串）裡的行程與項目搬進 Room
 *
 * - 項目只採用單獨存的那份（item:<id>），行程 JSON 內嵌的副本一律忽略：
 *   舊版的批次刪除與批次標記只更新單獨那份，內嵌副本可能是已刪除或過期的資料。
 * - 找不到所屬行程的項目、無法解析的單筆資料略過；讀取舊資料失敗則整個中止，不寫入任何資料。
 * - 依舊索引的順序寫入，時間相同的行程與項目維持原本的先後。
 * - 草稿仍留在鍵值儲存，不搬；舊版存下的路線也不搬，由啟動流程（`EnsureLocalDataReadyUseCase`）清掉。
 * - 舊資料搬完後保留，不刪除。
 */
class LegacyJsonImporter(
    private val storage: StorageService,
    private val database: AppDatabase,
) : LegacyDataImporter {

    override suspend fun importIfNeeded(): Result<Unit> = runCatching {
        if (storage.load(MARKER_KEY).getOrThrow() != null) return@runCatching
        if (database.itineraryDao().count() > 0) {
            markImported()
            return@runCatching
        }

        val keys = storage.getAllKeys().getOrThrow().toSet()
        val itineraries = loadEntries(keys, ITINERARY_PREFIX, ITINERARY_INDEX_KEY) { json ->
            JsonSerializer.deserializeItinerary(json).toDomain().toEntity()
        }
        val itineraryIds = itineraries.mapTo(HashSet()) { it.id }
        val items = loadEntries(keys, ITEM_PREFIX, ITEM_INDEX_KEY) { json ->
            JsonSerializer.deserializeItineraryItem(json).toDomain().toEntity()
        }.filter { it.itineraryId in itineraryIds }

        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                itineraries.forEach { database.itineraryDao().insert(it) }
                items.forEach { database.itineraryItemDao().insert(it) }
            }
        }
        markImported()
    }

    /**
     * 依舊索引的順序讀出某一類資料，再補上不在索引裡的鍵（依 id 排序）
     *
     * 索引本身損壞時只是失去順序，資料仍照鍵搬移。
     */
    private suspend fun <T> loadEntries(
        keys: Set<String>,
        prefix: String,
        indexKey: String,
        parse: (String) -> T,
    ): List<T> {
        val storedIds = keys.filter { it.startsWith(prefix) && it != indexKey }.map { it.removePrefix(prefix) }
        val indexedIds = storage.load(indexKey).getOrThrow()
            ?.let { runCatching { JsonSerializer.deserializeStringList(it) }.getOrNull() }
            .orEmpty()
        val orderedIds = (indexedIds.filter { it in storedIds } + storedIds.sorted()).distinct()

        return orderedIds.mapNotNull { id ->
            val json = storage.load(prefix + id).getOrThrow() ?: return@mapNotNull null
            runCatching { parse(json) }.getOrNull()
        }
    }

    /** 標記寫入失敗也無妨：下次啟動時資料庫已有資料，會直接補寫標記 */
    private suspend fun markImported() {
        storage.save(MARKER_KEY, "1")
    }

    companion object {
        const val MARKER_KEY = "migration:room:v1"
        private const val ITINERARY_PREFIX = "itinerary:"
        private const val ITINERARY_INDEX_KEY = "itinerary:index"
        private const val ITEM_PREFIX = "item:"
        private const val ITEM_INDEX_KEY = "item:index"
    }
}
