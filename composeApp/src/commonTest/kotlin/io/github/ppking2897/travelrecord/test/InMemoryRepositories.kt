@file:OptIn(ExperimentalTime::class)

package io.github.ppking2897.travelrecord.test

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.ItineraryItemRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.ExperimentalTime

/**
 * 存在記憶體的行程 Repository，給 ViewModel 測試組資料層用
 *
 * 行為與資料庫版相同：讀取時 `items` 從 [itemRepository] 帶出、寫入時忽略 `items`、刪除行程連帶刪除它的項目。
 * 跟資料庫版綁在同一份合約與驗收測試上驗證（InMemoryRepositoryContractTests）。
 * 加鎖是因為 ViewModel 會在背景執行緒產生縮圖，同時寫入資料。
 */
class InMemoryItineraryRepository(
    private val itemRepository: ItineraryItemRepository,
    vararg initial: Itinerary,
) : ItineraryRepository {
    private val lock = Mutex()
    private val itineraries = initial.map { it.copy(items = emptyList()) }.toMutableList()

    private suspend fun Itinerary.withItems(): Itinerary =
        copy(items = itemRepository.getItemsByItinerary(id).getOrThrow())

    override suspend fun createItinerary(itinerary: Itinerary): Result<Itinerary> = lock.withLock {
        itineraries += itinerary.copy(items = emptyList())
        Result.success(itinerary)
    }

    override suspend fun getItinerary(id: String): Result<Itinerary?> =
        Result.success(lock.withLock { itineraries.find { it.id == id } }?.withItems())

    override suspend fun getAllItineraries(): Result<List<Itinerary>> =
        Result.success(lock.withLock { itineraries.sortedByDescending { it.createdAt } }.map { it.withItems() })

    override suspend fun updateItinerary(itinerary: Itinerary): Result<Itinerary> = lock.withLock {
        val index = itineraries.indexOfFirst { it.id == itinerary.id }
        if (index < 0) return@withLock Result.failure(NoSuchElementException("找不到行程 ${itinerary.id}"))
        itineraries[index] = itinerary.copy(items = emptyList())
        Result.success(itinerary)
    }

    override suspend fun deleteItinerary(id: String): Result<Unit> {
        lock.withLock { itineraries.removeAll { it.id == id } }
        itemRepository.getItemsByItinerary(id).getOrThrow().forEach { itemRepository.deleteItem(it.id).getOrThrow() }
        return Result.success(Unit)
    }

    override suspend fun searchItineraries(query: String): Result<List<Itinerary>> {
        val all = getAllItineraries().getOrThrow()
        if (query.isBlank()) return Result.success(all)
        return Result.success(
            all.filter { itinerary ->
                itinerary.title.contains(query, ignoreCase = true) ||
                    itinerary.description.contains(query, ignoreCase = true) ||
                    itinerary.items.any { item ->
                        item.location.name.contains(query, ignoreCase = true) ||
                            item.activity.contains(query, ignoreCase = true)
                    }
            }
        )
    }
}

/**
 * 存在記憶體的項目 Repository，排序規則與資料庫版相同：日期、抵達或離開時間（沒有時間的排前面）、新增順序
 */
class InMemoryItineraryItemRepository(vararg initial: ItineraryItem) : ItineraryItemRepository {
    private val lock = Mutex()
    private val items = initial.toMutableList()

    override suspend fun addItem(item: ItineraryItem): Result<ItineraryItem> = lock.withLock {
        items += item
        Result.success(item)
    }

    override suspend fun updateItem(item: ItineraryItem): Result<ItineraryItem> = lock.withLock {
        val index = items.indexOfFirst { it.id == item.id }
        if (index < 0) return@withLock Result.failure(NoSuchElementException("找不到項目 ${item.id}"))
        items[index] = item
        Result.success(item)
    }

    override suspend fun deleteItem(id: String): Result<Unit> = lock.withLock {
        items.removeAll { it.id == id }
        Result.success(Unit)
    }

    override suspend fun getItem(id: String): Result<ItineraryItem?> = lock.withLock {
        Result.success(items.find { it.id == id })
    }

    override suspend fun getItemsByItinerary(itineraryId: String): Result<List<ItineraryItem>> = lock.withLock {
        Result.success(
            items.filter { it.itineraryId == itineraryId }
                .sortedWith(compareBy({ it.date }, { it.arrivalTime ?: it.departureTime }))
        )
    }
}

/** 項目存檔一律失敗，其餘行為同 [base] */
class FailingUpdateItemRepository(
    private val base: ItineraryItemRepository
) : ItineraryItemRepository by base {
    override suspend fun updateItem(item: ItineraryItem): Result<ItineraryItem> =
        Result.failure(IllegalStateException("存檔失敗"))
}
