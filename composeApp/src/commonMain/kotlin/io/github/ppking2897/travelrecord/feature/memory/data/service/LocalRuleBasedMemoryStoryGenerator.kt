@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.feature.memory.data.service

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.memory.domain.model.DailyHighlight
import io.github.ppking2897.travelrecord.feature.memory.domain.model.MemoryStory
import io.github.ppking2897.travelrecord.feature.memory.domain.service.MemoryStoryGenerator
import kotlinx.datetime.LocalDate

/**
 * 本地規則式 MemoryStoryGenerator
 *
 * 用模板與行程資料拼接繁體中文的溫暖敘事，完全離線、不呼叫外部 API。
 * 中文字與地名之間、句子之間不留空格；數字前後留空格，跟 App 其他地方的「3 個地點」一致。
 */
class LocalRuleBasedMemoryStoryGenerator : MemoryStoryGenerator {

    override suspend fun generate(
        itinerary: Itinerary,
        items: List<ItineraryItem>
    ): MemoryStory {
        val sortedItems = items.sortedWith(compareBy({ it.date }, { it.primaryTime() }))
        val dateGroups = sortedItems.groupBy { it.date }

        val placeCount = sortedItems.size
        val completedCount = sortedItems.count { it.isCompleted }
        val photoCount = sortedItems.sumOf { it.photos.size }
        val dayCount = dateGroups.size
        val startDate = itinerary.startDate
        val mainLocationName = sortedItems
            .groupingBy { it.location.name }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?.takeIf { it.isNotBlank() }
            ?: itinerary.title

        val firstLocation = sortedItems.firstOrNull()?.location?.name
        val lastLocation = sortedItems.lastOrNull()?.location?.name
        val seasonWord = startDate?.let { seasonOf(it.monthNumber) } ?: "旅行"

        return MemoryStory(
            title = buildTitle(seasonWord, mainLocationName),
            summary = buildSummary(startDate, mainLocationName, dayCount, placeCount),
            narrative = buildNarrative(
                startDate = startDate,
                seasonWord = seasonWord,
                mainLocationName = mainLocationName,
                firstLocation = firstLocation,
                lastLocation = lastLocation,
                dayCount = dayCount,
                placeCount = placeCount,
                completedCount = completedCount,
                photoCount = photoCount
            ),
            dailyHighlights = dateGroups.entries
                .sortedBy { it.key }
                .map { (date, dayItems) ->
                    val anchor = dayItems.firstOrNull { it.isCompleted } ?: dayItems.first()
                    DailyHighlight(
                        date = date,
                        headline = anchor.activity.ifBlank { anchor.location.name },
                        placeCount = dayItems.size
                    )
                }
        )
    }

    private fun buildTitle(seasonWord: String, mainLocation: String): String {
        return "${seasonWord}的$mainLocation"
    }

    private fun buildSummary(
        startDate: LocalDate?,
        mainLocation: String,
        dayCount: Int,
        placeCount: Int
    ): String {
        val monthText = startDate?.let { "${it.monthNumber} 月" } ?: "某個月份"
        return if (dayCount > 0 && placeCount > 0) {
            "${monthText}的${mainLocation}之旅，走了 $dayCount 天、$placeCount 個地方。"
        } else {
            "${monthText}的${mainLocation}之旅，留下一段屬於自己的節奏。"
        }
    }

    private fun buildNarrative(
        startDate: LocalDate?,
        seasonWord: String,
        mainLocationName: String,
        firstLocation: String?,
        lastLocation: String?,
        dayCount: Int,
        placeCount: Int,
        completedCount: Int,
        photoCount: Int
    ): String {
        val opening = when {
            startDate != null && firstLocation != null ->
                "${startDate.monthNumber} 月 ${startDate.dayOfMonth} 日，旅程從${firstLocation}展開。"
            firstLocation != null ->
                "從${firstLocation}出發，${seasonWord}的風一路陪你前行。"
            else ->
                "${seasonWord}的腳步，帶你走進${mainLocationName}。"
        }

        val middle = if (placeCount > 0) {
            val completionText = when {
                completedCount == placeCount -> "每一處都留下了打卡紀錄"
                completedCount == 0 -> "雖然沒能逐一打卡，但都走過了"
                else -> "其中 $completedCount 處完成了打卡，其他留給下一次的回望"
            }
            "這 $dayCount 天裡走過 $placeCount 個地方，$completionText。"
        } else {
            "整段旅程像一段安靜的散步，沒有固定的行程表。"
        }

        val closing = if (lastLocation != null && lastLocation != firstLocation) {
            val photoText = when {
                photoCount == 0 -> "沒有拍下照片，但每個場景都還留在記憶裡"
                photoCount < 5 -> "相簿裡留下 $photoCount 張照片，剩下的就放在心裡"
                else -> "相簿裡留下 $photoCount 張照片，足夠讓回憶細細展開"
            }
            "最後一站${lastLocation}成了旅程的句點——$photoText。"
        } else {
            "旅程結束時，城市仍在那裡，風景仍是風景。"
        }

        return opening + middle + closing
    }

    private fun seasonOf(month: Int): String = when (month) {
        3, 4, 5 -> "春天"
        6, 7, 8 -> "夏天"
        9, 10, 11 -> "秋天"
        12, 1, 2 -> "冬天"
        else -> "四季"
    }
}
