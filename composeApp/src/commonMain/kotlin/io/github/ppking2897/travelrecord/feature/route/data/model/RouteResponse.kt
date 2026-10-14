package com.example.myapplication.feature.route.data.model

import kotlinx.serialization.Serializable

/**
 * 路線資料傳輸物件
 */
@Serializable
data class RouteResponse(
    val id: String,
    val title: String,
    val locations: List<RouteLocationResponse>,
    val createdFrom: String
)
