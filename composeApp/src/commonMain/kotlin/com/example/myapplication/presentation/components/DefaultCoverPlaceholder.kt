package com.example.myapplication.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.myapplication.presentation.theme.AppGradients
import com.example.myapplication.presentation.theme.IconSize
import com.example.myapplication.presentation.theme.TravelRecordTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 預設封面佔位圖
 *
 * 當行程沒有封面照片時顯示，使用漸層背景搭配旅遊主題插圖
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
    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

    Box(
        modifier = modifier.background(brush = backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        // 背景旅遊主題插圖
        TravelIllustration(
            color = iconColor,
            modifier = Modifier.fillMaxSize()
        )

        // 主要相機圖示
        Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = null,
            modifier = Modifier.size(IconSize.xxl),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

/**
 * 旅遊主題插圖
 *
 * 使用 Canvas 繪製簡約旅遊圖案：飛機軌跡、山脈剪影
 */
@Composable
private fun TravelIllustration(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // 繪製山脈剪影（右下角）
        val mountainPath = Path().apply {
            // 第一座山（較小）
            moveTo(width * 0.5f, height)
            lineTo(width * 0.65f, height * 0.7f)
            lineTo(width * 0.8f, height)
            close()

            // 第二座山（較大，稍微重疊）
            moveTo(width * 0.6f, height)
            lineTo(width * 0.8f, height * 0.55f)
            lineTo(width, height * 0.75f)
            lineTo(width, height)
            close()
        }
        drawPath(
            path = mountainPath,
            color = color.copy(alpha = 0.4f)
        )

        // 繪製飛機軌跡（虛線弧線）
        val flightPath = Path().apply {
            moveTo(width * 0.1f, height * 0.7f)
            quadraticTo(
                width * 0.3f, height * 0.2f,
                width * 0.7f, height * 0.25f
            )
        }
        drawPath(
            path = flightPath,
            color = color,
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(8.dp.toPx(), 6.dp.toPx())
                ),
                cap = StrokeCap.Round
            )
        )

        // 繪製小飛機圖示（在軌跡終點）
        val airplaneX = width * 0.7f
        val airplaneY = height * 0.25f
        val planeSize = 12.dp.toPx()

        rotate(degrees = 25f, pivot = Offset(airplaneX, airplaneY)) {
            // 機身
            drawLine(
                color = color,
                start = Offset(airplaneX - planeSize, airplaneY),
                end = Offset(airplaneX + planeSize, airplaneY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            // 機翼
            drawLine(
                color = color,
                start = Offset(airplaneX, airplaneY - planeSize * 0.8f),
                end = Offset(airplaneX, airplaneY + planeSize * 0.8f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            // 尾翼
            drawLine(
                color = color,
                start = Offset(airplaneX - planeSize * 0.7f, airplaneY - planeSize * 0.4f),
                end = Offset(airplaneX - planeSize * 0.7f, airplaneY + planeSize * 0.4f),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // 繪製雲朵（左上角）
        val cloudY = height * 0.15f
        val cloudX = width * 0.2f
        val cloudRadius = 8.dp.toPx()

        // 雲朵由多個圓組成
        drawCircle(
            color = color.copy(alpha = 0.5f),
            radius = cloudRadius,
            center = Offset(cloudX, cloudY)
        )
        drawCircle(
            color = color.copy(alpha = 0.5f),
            radius = cloudRadius * 0.8f,
            center = Offset(cloudX - cloudRadius * 0.9f, cloudY + cloudRadius * 0.2f)
        )
        drawCircle(
            color = color.copy(alpha = 0.5f),
            radius = cloudRadius * 0.7f,
            center = Offset(cloudX + cloudRadius * 0.9f, cloudY + cloudRadius * 0.3f)
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
