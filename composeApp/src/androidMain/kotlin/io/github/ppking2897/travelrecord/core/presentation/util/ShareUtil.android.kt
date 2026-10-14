package io.github.ppking2897.travelrecord.core.presentation.util

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun shareLauncher(): ShareLauncher {
    val context = LocalContext.current
    return remember {
        object : ShareLauncher {
            override fun share(text: String, title: String?) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    if (title != null) {
                        putExtra(Intent.EXTRA_SUBJECT, title)
                    }
                }
                context.startActivity(Intent.createChooser(intent, title ?: "分享"))
            }
        }
    }
}
