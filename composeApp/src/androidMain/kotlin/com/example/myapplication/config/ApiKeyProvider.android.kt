package com.example.myapplication.config

import com.example.myapplication.BuildConfig

/**
 * Android 平台的 API Key 提供者
 *
 * 從 BuildConfig 讀取在 build.gradle.kts 中設定的 API Key
 */
actual object ApiKeyProvider {
    actual val googlePlacesApiKey: String = BuildConfig.GOOGLE_PLACES_API_KEY
}
