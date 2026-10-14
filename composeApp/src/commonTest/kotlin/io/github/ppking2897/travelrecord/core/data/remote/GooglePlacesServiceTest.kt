package io.github.ppking2897.travelrecord.core.data.remote

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
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

    fun recordingClient(requests: MutableList<HttpRequestData>, responseBody: String) =
        HttpClient(MockEngine { request ->
            requests += request
            respond(
                content = responseBody,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
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
