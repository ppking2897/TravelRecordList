package com.example.myapplication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.presentation.theme.AppGradients
import com.example.myapplication.presentation.theme.IconSize
import com.example.myapplication.presentation.theme.TravelRecordTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 預設封面佔位圖
 *
 * 當行程沒有封面照片時顯示，使用漸層背景搭配相機圖示
 *
 * @param modifier Modifier
 */
@Composable
fun DefaultCoverPlaceholder(
    modifier: Modifier = Modifier
) {
    val isLightTheme = !isSystemInDarkTheme()
    val backgroundGradient = if (isLightTheme) {
        AppGradients.warmBackgroundGradient
    } else {
        AppGradients.darkBackgroundGradient
    }

    Box(
        modifier = modifier.background(brush = backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = null,
            modifier = Modifier.size(IconSize.xxl),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Preview
@Composable
private fun DefaultCoverPlaceholderPreview() {
    TravelRecordTheme {
        Surface {
            DefaultCoverPlaceholder(
                modifier = Modifier.size(200.dp, 120.dp)
            )
        }
    }
}
