package io.github.ppking2897.travelrecord.core.data.remote

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.github.ppking2897.travelrecord.core.domain.service.LocationSuggestion
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.http.content.TextContent
import kotlinx.io.IOException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * GooglePlacesService 的請求內容
 *
 * key 設了 Android 應用程式限制後，Google 只接受帶有相符 X-Android-Package / X-Android-Cert 的請求，
 * 所以兩支 API 都必須附上 App 身分。
 */
class GooglePlacesServiceTest : StringSpec({

    val identity = object : AppIdentity {
        override val requestHeaders = mapOf(
            "X-Android-Package" to "com.test.app",
            "X-Android-Cert" to "0873D391E987982FBBD30873D391E987982FBBD3",
        )
    }

    // Json 設定與 CoreModule 的正式 HttpClient 相同
    fun recordingClient(
        requests: MutableList<HttpRequestData>,
        responseBody: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ) =
        HttpClient(MockEngine { request ->
            requests += request
            respond(
                content = responseBody,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }

    fun offlineClient() =
        HttpClient(MockEngine { throw IOException("沒有網路") }) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

    // 欄位名稱照 Google Places API (New) 的回應格式
    val kiyomizuJson = """
        {
          "id": "place-kiyomizu",
          "displayName": { "text": "清水寺", "languageCode": "zh-TW" },
          "formattedAddress": "日本京都府京都市東山區清水1丁目294",
          "location": { "latitude": 34.9948561, "longitude": 135.7850463 }
        }
    """.trimIndent()

    // 規格：選了建議會自動帶入地址與座標
    "搜尋結果帶出每個地點的名稱、地址與座標" {
        val service = GooglePlacesService(
            recordingClient(mutableListOf(), """{"places":[$kiyomizuJson]}"""),
            identity,
        ) { "test-key" }

        val suggestions = service.searchByName("清水寺").getOrThrow()

        suggestions shouldBe listOf(
            LocationSuggestion(
                placeId = "place-kiyomizu",
                name = "清水寺",
                address = "日本京都府京都市東山區清水1丁目294",
                latitude = 34.9948561,
                longitude = 135.7850463,
            )
        )
    }

    "查地點詳情帶出名稱、地址與座標" {
        val service = GooglePlacesService(
            recordingClient(mutableListOf(), kiyomizuJson),
            identity,
        ) { "test-key" }

        val details = service.getPlaceDetails("place-kiyomizu").getOrThrow()

        details.name shouldBe "清水寺"
        details.address shouldBe "日本京都府京都市東山區清水1丁目294"
        details.latitude shouldBe 34.9948561
        details.longitude shouldBe 135.7850463
    }

    // 規格：沒有網路、金鑰無效或其他錯誤時，建議清單不出現
    "沒有網路時搜尋回傳失敗，不丟出例外" {
        val service = GooglePlacesService(offlineClient(), identity) { "test-key" }

        service.searchByName("清水寺").isFailure shouldBe true
        service.getPlaceDetails("place-1").isFailure shouldBe true
    }

    "金鑰無效時搜尋沒有任何建議" {
        val errorBody = """{"error":{"code":403,"message":"API key not valid.","status":"PERMISSION_DENIED"}}"""
        val service = GooglePlacesService(
            recordingClient(mutableListOf(), errorBody, HttpStatusCode.Forbidden),
            identity,
        ) { "test-key" }

        service.searchByName("清水寺").getOrNull().orEmpty().shouldBeEmpty()
        service.getPlaceDetails("place-1").isFailure shouldBe true
    }

    "伺服器錯誤時搜尋沒有任何建議" {
        val service = GooglePlacesService(
            recordingClient(mutableListOf(), "Internal Server Error", HttpStatusCode.InternalServerError),
            identity,
        ) { "test-key" }

        service.searchByName("清水寺").getOrNull().orEmpty().shouldBeEmpty()
    }

    // 規格：建議以繁體中文顯示
    "中文關鍵字以繁體中文查詢" {
        val requests = mutableListOf<HttpRequestData>()
        val service = GooglePlacesService(recordingClient(requests, """{"places":[]}"""), identity) { "test-key" }

        service.searchByName("清水寺")

        val request = requests.single()
        request.headers["Accept-Language"] shouldBe "zh-TW"
        (request.body as TextContent).text shouldContain "\"languageCode\":\"zh-TW\""
    }

    // 2026-11-04 決定：不管輸入什麼語言，建議一律以繁體中文顯示
    "日文、韓文、英文關鍵字也以繁體中文查詢" {
        val requests = mutableListOf<HttpRequestData>()
        val service = GooglePlacesService(recordingClient(requests, """{"places":[]}"""), identity) { "test-key" }

        service.searchByName("きよみずでら")
        service.searchByName("경복궁")
        service.searchByName("Kiyomizu-dera")

        requests.size shouldBe 3
        requests.forEach { request ->
            request.headers["Accept-Language"] shouldBe "zh-TW"
            (request.body as TextContent).text shouldContain "\"languageCode\":\"zh-TW\""
        }
    }

    "搜尋地點的請求帶上 key 與 App 身分" {
        val requests = mutableListOf<HttpRequestData>()
        val service = GooglePlacesService(recordingClient(requests, """{"places":[]}"""), identity) { "test-key" }

        service.searchByName("清水寺")

        val headers = requests.single().headers
        headers["X-Goog-Api-Key"] shouldBe "test-key"
        headers["X-Android-Package"] shouldBe "com.test.app"
        headers["X-Android-Cert"] shouldBe "0873D391E987982FBBD30873D391E987982FBBD3"
    }

    "查地點詳情的請求帶上 key 與 App 身分" {
        val requests = mutableListOf<HttpRequestData>()
        val service = GooglePlacesService(recordingClient(requests, """{"id":"place-1"}"""), identity) { "test-key" }

        service.getPlaceDetails("place-1")

        val headers = requests.single().headers
        headers["X-Goog-Api-Key"] shouldBe "test-key"
        headers["X-Android-Package"] shouldBe "com.test.app"
        headers["X-Android-Cert"] shouldBe "0873D391E987982FBBD30873D391E987982FBBD3"
    }

    "沒有 key 時不發出請求、直接回傳失敗" {
        val requests = mutableListOf<HttpRequestData>()
        val service = GooglePlacesService(recordingClient(requests, "{}"), identity) { "" }

        service.searchByName("清水寺").isFailure shouldBe true
        service.getPlaceDetails("place-1").isFailure shouldBe true
        requests.shouldBeEmpty()
    }
})
