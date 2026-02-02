package com.example.myapplication.config

/**
 * iOS 平台的 API Key 提供者
 *
 * TODO: 從 Info.plist 讀取 API Key
 * 目前暫時回傳空字串，待 iOS 版本開發時實作
 */
actual object ApiKeyProvider {
    actual val googlePlacesApiKey: String = ""
}
