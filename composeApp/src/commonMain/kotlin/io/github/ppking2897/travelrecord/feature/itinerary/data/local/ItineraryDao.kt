package io.github.ppking2897.travelrecord.feature.itinerary.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ItineraryDao {

    /** 同 id 已存在時拋出例外；不用 REPLACE，因為它會先刪除舊列並連帶刪除行程的項目 */
    @Insert
    suspend fun insert(itinerary: ItineraryEntity)

    /** @return 更新到的列數，0 表示行程不存在 */
    @Update
    suspend fun update(itinerary: ItineraryEntity): Int

    @Query("DELETE FROM itineraries WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM itineraries WHERE id = :id")
    suspend fun get(id: String): ItineraryEntity?

    /** 依建立時間由新到舊；時間相同時保留新增順序 */
    @Query("SELECT * FROM itineraries ORDER BY createdAt DESC, rowid")
    suspend fun getAll(): List<ItineraryEntity>

    @Query("SELECT COUNT(*) FROM itineraries")
    suspend fun count(): Int
}
