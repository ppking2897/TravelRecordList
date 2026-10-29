package io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlin.test.Test

/**
 * 從備註抓標籤，測試名稱對應 .claude/docs/SpecAdjustments.md 第 6 組的 Scenario
 */
class HashtagRepositoryImplTest {

    private val repository = HashtagRepositoryImpl()

    @Test
    fun `全形的＃也算標籤`() {
        val tags = repository.extractHashtags("排隊 30 分＃拉麵 #宵夜").map { it.tag }

        tags shouldContainExactlyInAnyOrder listOf("拉麵", "宵夜")
    }
}
