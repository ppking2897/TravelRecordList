package com.example.myapplication.domain.entity

/**
 * 可分享的旅遊路線領域模型
 *
 * @property id 唯一識別碼
 * @property title 標題
 * @property locations 路線地點列表
 * @property createdFrom 來源行程 ID
 */
data class Route(
    val id: String,
    val title: String,
    val locations: List<RouteLocation>,
    val createdFrom: String
)
