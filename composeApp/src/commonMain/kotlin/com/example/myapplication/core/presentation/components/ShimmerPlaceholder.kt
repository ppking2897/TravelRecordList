package com.example.myapplication.core.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import com.example.myapplication.core.presentation.theme.CornerRadius

/**
 * 閃爍骨架動畫。以透明度在 surfaceVariant 上來回漸變，模擬載入中的內容佔位。
 *
 * KMP-friendly：不依賴特定平台繪圖 API，只用 Compose 的 infiniteTransition。
 */
@Composable
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
}

/**
 * 單行骨架佔位。用於模擬文字行。
 */
@Composable
fun SkeletonLine(
    height: Dp,
    modifier: Modifier = Modifier,
    width: Dp? = null,
    cornerRadius: Dp = CornerRadius.xs
) {
    Box(
        modifier = modifier
            .let { if (width != null) it.width(width) else it.fillMaxWidth() }
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .shimmer()
    )
}
