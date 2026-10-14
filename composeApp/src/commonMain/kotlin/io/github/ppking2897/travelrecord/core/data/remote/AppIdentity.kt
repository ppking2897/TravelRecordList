package io.github.ppking2897.travelrecord.core.data.remote

/**
 * 呼叫 Google API 時附上的 App 身分
 *
 * 對應 API key 的「應用程式限制」：key 設了限制後，Google 只接受身分相符的請求。
 * 一把 key 只能設一種平台的限制，所以各平台各自提供自己的 header。
 */
interface AppIdentity {
    val requestHeaders: Map<String, String>
}
