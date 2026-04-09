package com.example.myapplication.feature.route.data.dto

import kotlinx.serialization.Serializable

/**
 * 路線資料傳輸物件
 */
@Serializable
data class RouteDto(
    val id: String,
    val title: String,
    val locations: List<RouteLocationDto>,
    val createdFrom: String
)
