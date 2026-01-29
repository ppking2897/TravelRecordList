package com.example.myapplication.domain.entity

/**
 * 地點領域模型
 *
 * @property name 地點名稱（必填）
 * @property latitude 緯度（可選，範圍 -90 到 90）
 * @property longitude 經度（可選，範圍 -180 到 180）
 * @property address 地址（可選）
 */
data class Location(
    val name: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null
)
