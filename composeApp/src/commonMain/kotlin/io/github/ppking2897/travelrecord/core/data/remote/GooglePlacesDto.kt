package io.github.ppking2897.travelrecord.core.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Google Places API Text Search 請求
 */
@Serializable
data class GoogleTextSearchRequest(
    val textQuery: String,
    // 不給預設值：kotlinx-serialization 預設不輸出等於預設值的欄位，會讓 Google 收不到語言與筆數
    val languageCode: String,
    val pageSize: Int,
)

/**
 * Google Places API Text Search 回應
 */
@Serializable
data class GoogleTextSearchResponse(
    val places: List<GooglePlace> = emptyList(),
)

/**
 * Google Place 資料
 */
@Serializable
data class GooglePlace(
    val id: String,
    val displayName: GoogleDisplayName? = null,
    val formattedAddress: String? = null,
    val location: GoogleLocation? = null,
    val nationalPhoneNumber: String? = null,
    val websiteUri: String? = null,
)

/**
 * Google Place 顯示名稱
 */
@Serializable
data class GoogleDisplayName(
    val text: String,
    val languageCode: String? = null,
)

/**
 * Google Place 位置座標
 */
@Serializable
data class GoogleLocation(
    val latitude: Double,
    val longitude: Double,
)
