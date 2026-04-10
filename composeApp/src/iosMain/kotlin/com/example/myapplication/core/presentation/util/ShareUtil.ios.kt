package com.example.myapplication.core.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@Composable
actual fun shareLauncher(): ShareLauncher {
    return remember {
        object : ShareLauncher {
            override fun share(text: String, title: String?) {
                val activityItems = listOf(text)
                val activityController = UIActivityViewController(
                    activityItems = activityItems,
                    applicationActivities = null
                )
                val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
                rootViewController?.presentViewController(
                    activityController,
                    animated = true,
                    completion = null
                )
            }
        }
    }
}
