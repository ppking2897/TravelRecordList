package com.example.myapplication.config

/**
 * API Key 提供者
 *
 * 各平台提供不同的 API Key 取得方式：
 * - Android: 從 BuildConfig 讀取
 * - iOS: 從 Info.plist 讀取（暫未實作）
 */
expect object ApiKeyProvider {
    /**
     * 取得 Google Places API Key
     */
    val googlePlacesApiKey: String
}
