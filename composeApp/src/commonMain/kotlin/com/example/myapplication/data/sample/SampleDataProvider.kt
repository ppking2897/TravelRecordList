@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.data.sample

import com.example.myapplication.domain.entity.Itinerary
import com.example.myapplication.domain.entity.ItineraryItem
import com.example.myapplication.domain.entity.Location
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * 預設資料提供者
 *
 * 提供初次安裝時的範例行程資料
 */
object SampleDataProvider {

    private const val SAMPLE_ITINERARY_ID = "sample-kyoto-osaka-trip"

    /**
     * 取得範例行程
     */
    fun getSampleItinerary(): Itinerary {
        val now = kotlin.time.Clock.System.now()
        val startDate = LocalDate(2025, 4, 1)
        val endDate = LocalDate(2025, 4, 5)

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
                date = LocalDate(startDate.year, startDate.monthNumber, startDate.dayOfMonth + 1),
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
                date = LocalDate(startDate.year, startDate.monthNumber, startDate.dayOfMonth + 1),
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
                date = LocalDate(startDate.year, startDate.monthNumber, startDate.dayOfMonth + 2),
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
                date = LocalDate(startDate.year, startDate.monthNumber, startDate.dayOfMonth + 3),
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
                date = LocalDate(startDate.year, startDate.monthNumber, startDate.dayOfMonth + 3),
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
                date = LocalDate(startDate.year, startDate.monthNumber, startDate.dayOfMonth + 4),
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
