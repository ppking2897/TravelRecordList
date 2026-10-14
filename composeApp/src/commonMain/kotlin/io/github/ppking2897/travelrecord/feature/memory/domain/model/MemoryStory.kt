package com.example.myapplication.feature.memory.domain.model

import kotlinx.datetime.LocalDate

data class MemoryStory(
    val title: String,
    val summary: String,
    val narrative: String,
    val dailyHighlights: List<DailyHighlight>
)

data class DailyHighlight(
    val date: LocalDate,
    val headline: String,
    val placeCount: Int
)
