package io.github.ppking2897.travelrecord.config

/**
 * iOS 平台的 API Key 提供者
 *
 * TODO(等外部): 從 Info.plist 讀取 API Key（iOS 要另一把設 iOS 應用程式限制的 key），並在 IosPlatformModule 的 AppIdentity 附上 X-Ios-Bundle-Identifier；移除條件：在 Mac 建置 iOS，地點搜尋回傳結果
 * 目前回傳空字串，iOS 的地點搜尋一定失敗
 */
actual object ApiKeyProvider {
    actual val googlePlacesApiKey: String = ""
}
