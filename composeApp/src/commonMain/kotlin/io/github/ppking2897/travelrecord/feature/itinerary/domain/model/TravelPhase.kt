package com.example.myapplication.feature.itinerary.domain.model

enum class TravelPhase(
    val label: String
) {
    Planning("規劃中"),
    InProgress("旅行中"),
    Completed("已結束")
}
