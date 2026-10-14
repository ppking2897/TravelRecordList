package io.github.ppking2897.travelrecord.core.presentation.util

import androidx.compose.runtime.Composable

/**
 * 平台分享功能（Android: Intent.ACTION_SEND、iOS: UIActivityViewController）
 */
@Composable
expect fun shareLauncher(): ShareLauncher

interface ShareLauncher {
    fun share(text: String, title: String? = null)
}
