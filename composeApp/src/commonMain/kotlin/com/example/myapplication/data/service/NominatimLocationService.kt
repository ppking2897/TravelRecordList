package com.example.myapplication.data.service

import com.example.myapplication.domain.service.LocationDetails
import com.example.myapplication.domain.service.LocationSearchService
import com.example.myapplication.domain.service.LocationSuggestion
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OpenStreetMap Nominatim API 實作
 *
 * 使用 Nominatim 提供的免費地理編碼服務
 * API 文檔: https://nominatim.org/release-docs/develop/api/Search/
 *
 * 注意事項:
 * - 需要設置 User-Agent header
 * - 有速率限制（最多 1 request/second）
 * - 僅供非商業用途
 */
class NominatimLocationService(
    private val httpClient: HttpClient,
) : LocationSearchService {

    companion object {
        private const val BASE_URL = "https://nominatim.openstreetmap.org"
        private const val USER_AGENT = "TravelRecordApp/1.0"
    }

    override suspend fun searchByName(query: String): Result<List<LocationSuggestion>> {
        return runCatching {
            val response: List<NominatimSearchResult> = httpClient.get("$BASE_URL/search") {
                header("User-Agent", USER_AGENT)
                header("Accept-Language", "ja,zh-TW,zh,en")
                parameter("q", query)
                parameter("format", "json")
                parameter("addressdetails", "1")
                parameter("namedetails", "1")
                parameter("limit", "10")
            }.body()

            response.map { it.toLocationSuggestion() }
        }
    }

    override suspend fun getPlaceDetails(placeId: String): Result<LocationDetails> {
        return runCatching {
            val response: List<NominatimSearchResult> = httpClient.get("$BASE_URL/lookup") {
                header("User-Agent", USER_AGENT)
                parameter("osm_ids", placeId)
                parameter("format", "json")
                parameter("addressdetails", "1")
            }.body()

            response.firstOrNull()?.toLocationDetails()
                ?: throw NoSuchElementException("Place not found: $placeId")
        }
    }
}

/**
 * Nominatim API 搜尋結果
 */
@Serializable
private data class NominatimSearchResult(
    @SerialName("place_id")
    val placeId: Long,
    @SerialName("osm_type")
    val osmType: String? = null,
    @SerialName("osm_id")
    val osmId: Long? = null,
    @SerialName("display_name")
    val displayName: String,
    @SerialName("lat")
    val latitude: String,
    @SerialName("lon")
    val longitude: String,
    @SerialName("name")
    val name: String? = null,
    @SerialName("address")
    val address: NominatimAddress? = null,
    @SerialName("namedetails")
    val namedetails: NominatimNameDetails? = null,
) {
    fun toLocationSuggestion(): LocationSuggestion {
        val osmIdString = if (osmType != null && osmId != null) {
            "${osmType.first().uppercaseChar()}$osmId"
        } else {
            placeId.toString()
        }

        // 優先使用日文名稱，其次是原始名稱，最後從地址或顯示名稱中提取
        val bestName = namedetails?.extractBestName()
            ?: name
            ?: address?.extractName()
            ?: displayName.split(",").first()

        return LocationSuggestion(
            placeId = osmIdString,
            name = bestName,
            address = displayName,
            latitude = latitude.toDoubleOrNull(),
            longitude = longitude.toDoubleOrNull(),
        )
    }

    fun toLocationDetails(): LocationDetails {
        val osmIdString = if (osmType != null && osmId != null) {
            "${osmType.first().uppercaseChar()}$osmId"
        } else {
            placeId.toString()
        }

        val bestName = namedetails?.extractBestName()
            ?: name
            ?: address?.extractName()
            ?: displayName.split(",").first()

        return LocationDetails(
            placeId = osmIdString,
            name = bestName,
            address = displayName,
            latitude = latitude.toDoubleOrNull() ?: 0.0,
            longitude = longitude.toDoubleOrNull() ?: 0.0,
            phoneNumber = null,
            website = null,
        )
    }
}

/**
 * Nominatim 地址結構
 */
@Serializable
private data class NominatimAddress(
    val amenity: String? = null,
    val building: String? = null,
    val shop: String? = null,
    val tourism: String? = null,
    val road: String? = null,
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val county: String? = null,
    val state: String? = null,
    val country: String? = null,
) {
    fun extractName(): String? {
        return amenity ?: building ?: shop ?: tourism
    }
}

/**
 * Nominatim 名稱詳情結構
 *
 * 包含各種語言的地名，優先使用日文名稱以提高搜尋準確度
 */
@Serializable
private data class NominatimNameDetails(
    val name: String? = null,
    @SerialName("name:ja")
    val nameJa: String? = null,
    @SerialName("name:ja-Hira")
    val nameJaHira: String? = null,
    @SerialName("name:zh")
    val nameZh: String? = null,
    @SerialName("name:zh-TW")
    val nameZhTw: String? = null,
    @SerialName("name:en")
    val nameEn: String? = null,
) {
    /**
     * 提取最佳名稱，優先順序：日文 > 繁體中文 > 中文 > 原始名稱 > 英文
     */
    fun extractBestName(): String? {
        return nameJa ?: nameZhTw ?: nameZh ?: name ?: nameEn
    }
}
