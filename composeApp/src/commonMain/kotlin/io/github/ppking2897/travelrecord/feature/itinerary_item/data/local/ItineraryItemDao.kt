package io.github.ppking2897.travelrecord.feature.itinerary_item.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * 項目一律依日期、再依抵達或離開時間排序；沒有時間的排在當天最後，相同時保留新增順序
 *
 * 跟 domain 的 `itemDayOrder` 是同一套規則，畫面再排一次結果不變。
 */
@Dao
interface ItineraryItemDao {

    @Insert
    suspend fun insert(item: ItineraryItemEntity)

    /** @return 更新到的列數，0 表示項目不存在 */
    @Update
    suspend fun update(item: ItineraryItemEntity): Int

    @Query("DELETE FROM itinerary_items WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM itinerary_items WHERE id = :id")
    suspend fun get(id: String): ItineraryItemEntity?

    @Query(
        "SELECT * FROM itinerary_items WHERE itineraryId = :itineraryId ORDER BY $DAY_ORDER"
    )
    suspend fun getByItinerary(itineraryId: String): List<ItineraryItemEntity>

    @Query("SELECT * FROM itinerary_items ORDER BY $DAY_ORDER")
    suspend fun getAll(): List<ItineraryItemEntity>
}

/** `IS NULL` 在 SQLite 是 0／1，有時間的（0）排前面 */
private const val DAY_ORDER =
    "date, COALESCE(arrivalTime, departureTime) IS NULL, COALESCE(arrivalTime, departureTime), rowid"
