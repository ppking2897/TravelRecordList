package com.example.myapplication.data.service

import com.example.myapplication.config.ApiKeyProvider
import com.example.myapplication.data.service.dto.GooglePlace
import com.example.myapplication.data.service.dto.GoogleTextSearchRequest
import com.example.myapplication.data.service.dto.GoogleTextSearchResponse
import com.example.myapplication.domain.service.LocationDetails
import com.example.myapplication.domain.service.LocationSearchService
import com.example.myapplication.domain.service.LocationSuggestion
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Google Places API (New) 實作
 *
 * 使用 Google Places API 提供地點搜尋服務
 * API 文檔: https://developers.google.com/maps/documentation/places/web-service/text-search
 *
 * 注意事項:
 * - 需要有效的 API Key
 * - 每月有 $200 免費額度
 * - 使用 Field Mask 減少回傳資料量以節省費用
 */
class GooglePlacesService(
    private val httpClient: HttpClient,
) : LocationSearchService {

    companion object {
        private const val BASE_URL = "https://places.googleapis.com/v1"

        // Text Search 需要的欄位
        private const val TEXT_SEARCH_FIELD_MASK =
            "places.id,places.displayName,places.formattedAddress,places.location"

        // Place Details 需要的欄位（包含電話和網站）
        private const val PLACE_DETAILS_FIELD_MASK =
            "id,displayName,formattedAddress,location,nationalPhoneNumber,websiteUri"
    }

    private val apiKey: String
        get() = ApiKeyProvider.googlePlacesApiKey

    override suspend fun searchByName(query: String): Result<List<LocationSuggestion>> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("Google Places API Key is not configured"))
        }

        return runCatching {
            val lang = detectLanguage(query)
            val response: GoogleTextSearchResponse = httpClient.post("$BASE_URL/places:searchText") {
                header("X-Goog-Api-Key", apiKey)
                header("X-Goog-FieldMask", TEXT_SEARCH_FIELD_MASK)
                header("Accept-Language", lang)
                contentType(ContentType.Application.Json)
                setBody(
                    GoogleTextSearchRequest(
                        textQuery = query,
                        languageCode = lang,
                    )
                )
            }.body()

            response.places.map { it.toLocationSuggestion() }
        }
    }

    override suspend fun getPlaceDetails(placeId: String): Result<LocationDetails> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("Google Places API Key is not configured"))
        }

        return runCatching {
            val response: GooglePlace = httpClient.get("$BASE_URL/places/$placeId") {
                header("X-Goog-Api-Key", apiKey)
                header("X-Goog-FieldMask", PLACE_DETAILS_FIELD_MASK)
            }.body()

            response.toLocationDetails()
        }
    }
}

/**
 * 根據輸入文字自動檢測語言
 *
 * - 包含平假名/片假名 → 日文 (ja)
 * - 包含韓文 → 韓文 (ko)
 * - 包含中文字符（無日文假名）→ 繁體中文 (zh-TW)
 * - 其他 → 英文 (en)
 */
private fun detectLanguage(text: String): String {
    val hasHiragana = text.any { it in '\u3040'..'\u309F' }
    val hasKatakana = text.any { it in '\u30A0'..'\u30FF' }
    val hasKorean = text.any { it in '\uAC00'..'\uD7AF' }
    val hasChinese = text.any { it in '\u4E00'..'\u9FFF' }

    return when {
        hasHiragana || hasKatakana -> "ja"
        hasKorean -> "ko"
        hasChinese -> "zh-TW"
        else -> "en"
    }
}

/**
 * 將 GooglePlace 轉換為 LocationSuggestion
 */
private fun GooglePlace.toLocationSuggestion(): LocationSuggestion {
    return LocationSuggestion(
        placeId = id,
        name = displayName?.text ?: "",
        address = formattedAddress ?: "",
        latitude = location?.latitude,
        longitude = location?.longitude,
    )
}

/**
 * 將 GooglePlace 轉換為 LocationDetails
 */
private fun GooglePlace.toLocationDetails(): LocationDetails {
    return LocationDetails(
        placeId = id,
        name = displayName?.text ?: "",
        address = formattedAddress ?: "",
        latitude = location?.latitude ?: 0.0,
        longitude = location?.longitude ?: 0.0,
        phoneNumber = nationalPhoneNumber,
        website = websiteUri,
    )
}
