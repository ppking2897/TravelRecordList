@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.core.domain.sample

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Location
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Photo
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * 預設資料提供者
 *
 * 提供初次安裝時的範例行程資料。起訖日期以「今天」為基準，
 * 讓「今日」分頁一安裝就能看到內容。
 */
object SampleDataProvider {

    private const val SAMPLE_ITINERARY_ID = "sample-kyoto-osaka-trip"
    private const val SAMPLE_PAST_ITINERARY_ID = "sample-tokyo-trip"

    /**
     * 取得範例行程（現在正在旅行中）
     */
    fun getSampleItinerary(): Itinerary {
        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val startDate = today.plus(-1, DateTimeUnit.DAY)
        val endDate = today.plus(3, DateTimeUnit.DAY)

        return Itinerary(
            id = SAMPLE_ITINERARY_ID,
            title = "京都大阪 5 日遊",
            description = "春季賞櫻之旅，走訪京都古寺與大阪美食",
            startDate = startDate,
            endDate = endDate,
            coverPhotoPath = null,
            items = getSampleItems(SAMPLE_ITINERARY_ID, startDate),
            createdAt = now,
            modifiedAt = now
        )
    }

    /**
     * 取得已結束的範例行程（上個月的東京之旅），用來讓「回憶」分頁一
     * 安裝就有內容可以看。
     */
    fun getSamplePastItinerary(): Itinerary {
        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val startDate = today.plus(-30, DateTimeUnit.DAY)
        val endDate = today.plus(-28, DateTimeUnit.DAY)

        return Itinerary(
            id = SAMPLE_PAST_ITINERARY_ID,
            title = "東京 3 日遊",
            description = "上個月的東京輕旅行",
            startDate = startDate,
            endDate = endDate,
            coverPhotoPath = null,
            items = getSamplePastItems(SAMPLE_PAST_ITINERARY_ID, startDate),
            createdAt = now,
            modifiedAt = now
        )
    }

    private fun getSamplePastItems(itineraryId: String, startDate: LocalDate): List<ItineraryItem> {
        val now = Clock.System.now()
        return listOf(
            ItineraryItem(
                id = "sample-past-1",
                itineraryId = itineraryId,
                date = startDate,
                arrivalTime = LocalTime(10, 0),
                departureTime = LocalTime(12, 30),
                location = Location(
                    name = "淺草寺",
                    address = "東京都台東区浅草2-3-1",
                    latitude = 35.7148,
                    longitude = 139.7967
                ),
                activity = "雷門參拜與仲見世通散步",
                notes = "買了人形燒當下午茶",
                isCompleted = true,
                completedAt = now,
                createdAt = now,
                modifiedAt = now
            ),
            ItineraryItem(
                id = "sample-past-2",
                itineraryId = itineraryId,
                date = startDate,
                arrivalTime = LocalTime(15, 0),
                departureTime = LocalTime(18, 0),
                location = Location(
                    name = "晴空塔",
                    address = "東京都墨田区押上1-1-2",
                    latitude = 35.7101,
                    longitude = 139.8107
                ),
                activity = "晴空塔展望台俯瞰夜景",
                notes = "黃昏時段光線最好",
                isCompleted = true,
                completedAt = now,
                createdAt = now,
                modifiedAt = now
            ),
            ItineraryItem(
                id = "sample-past-3",
                itineraryId = itineraryId,
                date = startDate.plus(1, DateTimeUnit.DAY),
                arrivalTime = LocalTime(9, 30),
                departureTime = LocalTime(11, 30),
                location = Location(
                    name = "明治神宮",
                    address = "東京都渋谷区代々木神園町1-1",
                    latitude = 35.6764,
                    longitude = 139.6993
                ),
                activity = "林蔭參道散步",
                notes = "早上人少，很適合拍照",
                photos = listOf(
                    Photo("sample-past-3-photo-1", "sample-past-3", "demo1.jpg", "", order = 0, fileSize = 0L, uploadedAt = now, modifiedAt = now),
                    Photo("sample-past-3-photo-2", "sample-past-3", "demo2.jpg", "", order = 1, fileSize = 0L, uploadedAt = now, modifiedAt = now),
                    Photo("sample-past-3-photo-3", "sample-past-3", "demo3.jpg", "", order = 2, fileSize = 0L, uploadedAt = now, modifiedAt = now)
                ),
                isCompleted = true,
                completedAt = now,
                createdAt = now,
                modifiedAt = now
            ),
            ItineraryItem(
                id = "sample-past-4",
                itineraryId = itineraryId,
                date = startDate.plus(1, DateTimeUnit.DAY),
                arrivalTime = LocalTime(13, 0),
                departureTime = LocalTime(17, 0),
                location = Location(
                    name = "原宿竹下通",
                    address = "東京都渋谷区神宮前1丁目",
                    latitude = 35.6703,
                    longitude = 139.7026
                ),
                activity = "逛街吃可麗餅",
                notes = "必試彩虹起司熱狗",
                isCompleted = true,
                completedAt = now,
                createdAt = now,
                modifiedAt = now
            ),
            ItineraryItem(
                id = "sample-past-5",
                itineraryId = itineraryId,
                date = startDate.plus(2, DateTimeUnit.DAY),
                arrivalTime = LocalTime(10, 0),
                departureTime = LocalTime(15, 0),
                location = Location(
                    name = "秋葉原",
                    address = "東京都千代田区外神田",
                    latitude = 35.7023,
                    longitude = 139.7745
                ),
                activity = "逛電器街與動漫周邊",
                notes = "回國前最後一站",
                isCompleted = false,
                completedAt = null,
                createdAt = now,
                modifiedAt = now
            )
        )
    }

    /**
     * 取得範例行程項目
     */
    private fun getSampleItems(itineraryId: String, startDate: LocalDate): List<ItineraryItem> {
        val now = kotlin.time.Clock.System.now()

        return listOf(
            // Day 1 - 京都
            ItineraryItem(
                id = "sample-item-1",
                itineraryId = itineraryId,
                date = startDate,
                arrivalTime = LocalTime(9, 0),
                departureTime = LocalTime(12, 0),
                location = Location(
                    name = "伏見稲荷大社",
                    address = "京都府京都市伏見区深草藪之内町68",
                    latitude = 34.9671,
                    longitude = 135.7727
                ),
                activity = "千本鳥居參拜",
                notes = "建議早上前往避開人潮，穿舒適的鞋子",
                createdAt = now,
                modifiedAt = now
            ),
            ItineraryItem(
                id = "sample-item-2",
                itineraryId = itineraryId,
                date = startDate,
                arrivalTime = LocalTime(14, 0),
                departureTime = LocalTime(17, 0),
                location = Location(
                    name = "清水寺",
                    address = "京都府京都市東山区清水1丁目294",
                    latitude = 34.9949,
                    longitude = 135.7850
                ),
                activity = "清水舞台賞景",
                notes = "春季櫻花盛開，記得拍照",
                createdAt = now,
                modifiedAt = now
            ),

            // Day 2 - 京都
            ItineraryItem(
                id = "sample-item-3",
                itineraryId = itineraryId,
                date = startDate.plus(1, DateTimeUnit.DAY),
                arrivalTime = LocalTime(9, 30),
                departureTime = LocalTime(11, 30),
                location = Location(
                    name = "金閣寺",
                    address = "京都府京都市北区金閣寺町1",
                    latitude = 35.0394,
                    longitude = 135.7292
                ),
                activity = "參觀金閣寺",
                notes = "世界文化遺產，金色倒影很美",
                createdAt = now,
                modifiedAt = now
            ),
            ItineraryItem(
                id = "sample-item-4",
                itineraryId = itineraryId,
                date = startDate.plus(1, DateTimeUnit.DAY),
                arrivalTime = LocalTime(13, 0),
                departureTime = LocalTime(16, 0),
                location = Location(
                    name = "嵐山竹林",
                    address = "京都府京都市右京区嵯峨天龍寺芒ノ馬場町",
                    latitude = 35.0173,
                    longitude = 135.6717
                ),
                activity = "竹林散步",
                notes = "可順便搭嵐山小火車",
                createdAt = now,
                modifiedAt = now
            ),

            // Day 3 - 奈良
            ItineraryItem(
                id = "sample-item-5",
                itineraryId = itineraryId,
                date = startDate.plus(2, DateTimeUnit.DAY),
                arrivalTime = LocalTime(10, 0),
                departureTime = LocalTime(16, 0),
                location = Location(
                    name = "奈良公園",
                    address = "奈良県奈良市雑司町469",
                    latitude = 34.6851,
                    longitude = 135.8430
                ),
                activity = "餵鹿、東大寺參拜",
                notes = "買鹿仙貝餵小鹿，注意不要被鹿追",
                createdAt = now,
                modifiedAt = now
            ),

            // Day 4 - 大阪
            ItineraryItem(
                id = "sample-item-6",
                itineraryId = itineraryId,
                date = startDate.plus(3, DateTimeUnit.DAY),
                arrivalTime = LocalTime(10, 0),
                departureTime = LocalTime(13, 0),
                location = Location(
                    name = "大阪城",
                    address = "大阪府大阪市中央区大阪城1-1",
                    latitude = 34.6873,
                    longitude = 135.5262
                ),
                activity = "大阪城天守閣",
                notes = "登頂可俯瞰大阪市景",
                createdAt = now,
                modifiedAt = now
            ),
            ItineraryItem(
                id = "sample-item-7",
                itineraryId = itineraryId,
                date = startDate.plus(3, DateTimeUnit.DAY),
                arrivalTime = LocalTime(17, 0),
                departureTime = LocalTime(21, 0),
                location = Location(
                    name = "道頓堀",
                    address = "大阪府大阪市中央区道頓堀",
                    latitude = 34.6687,
                    longitude = 135.5015
                ),
                activity = "逛街吃美食",
                notes = "必吃：章魚燒、大阪燒、串炸",
                createdAt = now,
                modifiedAt = now
            ),

            // Day 5 - 大阪
            ItineraryItem(
                id = "sample-item-8",
                itineraryId = itineraryId,
                date = startDate.plus(4, DateTimeUnit.DAY),
                arrivalTime = LocalTime(10, 0),
                departureTime = LocalTime(18, 0),
                location = Location(
                    name = "日本環球影城",
                    address = "大阪府大阪市此花区桜島2丁目1-33",
                    latitude = 34.6654,
                    longitude = 135.4323
                ),
                activity = "環球影城一日遊",
                notes = "建議購買快速通關券",
                createdAt = now,
                modifiedAt = now
            )
        )
    }

    /**
     * 檢查是否為範例行程 ID
     */
    fun isSampleItineraryId(id: String): Boolean = id == SAMPLE_ITINERARY_ID
}
