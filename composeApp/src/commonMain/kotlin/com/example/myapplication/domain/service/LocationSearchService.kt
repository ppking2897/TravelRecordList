package com.example.myapplication.domain.service

/**
 * 地點搜尋服務介面
 *
 * 提供地點名稱搜尋和詳細資訊查詢功能
 */
interface LocationSearchService {
    /**
     * 根據名稱搜尋地點
     *
     * @param query 搜尋關鍵字
     * @return 搜尋結果列表
     */
    suspend fun searchByName(query: String): Result<List<LocationSuggestion>>

    /**
     * 取得地點詳細資訊
     *
     * @param placeId 地點 ID
     * @return 地點詳細資訊
     */
    suspend fun getPlaceDetails(placeId: String): Result<LocationDetails>
}

/**
 * 地點搜尋建議
 *
 * @property placeId 地點唯一識別碼
 * @property name 地點名稱
 * @property address 地址
 * @property latitude 緯度（可選，取決於搜尋 API）
 * @property longitude 經度（可選，取決於搜尋 API）
 */
data class LocationSuggestion(
    val placeId: String,
    val name: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
)

/**
 * 地點詳細資訊
 *
 * @property placeId 地點唯一識別碼
 * @property name 地點名稱
 * @property address 完整地址
 * @property latitude 緯度
 * @property longitude 經度
 * @property phoneNumber 電話號碼（可選）
 * @property website 網站（可選）
 */
data class LocationDetails(
    val placeId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phoneNumber: String? = null,
    val website: String? = null,
)
