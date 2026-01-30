package com.example.myapplication.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.presentation.theme.CornerRadius
import com.example.myapplication.presentation.theme.IconSize
import com.example.myapplication.presentation.theme.Spacing
import com.example.myapplication.presentation.theme.TravelRecordTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 封面照片選擇區塊
 *
 * 用於新增/編輯行程時選擇封面照片
 *
 * @param coverPhotoPath 當前封面照片路徑，null 表示無封面
 * @param onSelectPhoto 點擊選擇照片的回調
 * @param onRemovePhoto 移除照片的回調
 * @param modifier Modifier
 */
@Composable
fun CoverPhotoSection(
    coverPhotoPath: String?,
    onSelectPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            text = "封面照片",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clickable(onClick = onSelectPhoto),
            shape = RoundedCornerShape(CornerRadius.md),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (coverPhotoPath != null) {
                    // 顯示已選擇的封面照片
                    LocalImage(
                        filePath = coverPhotoPath,
                        contentDescription = "封面照片",
                        modifier = Modifier.fillMaxSize()
                    )

                    // 移除按鈕
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(Spacing.sm),
                        shape = RoundedCornerShape(CornerRadius.full),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ) {
                        IconButton(
                            onClick = onRemovePhoto,
                            modifier = Modifier.size(IconSize.md)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "移除封面",
                                modifier = Modifier.size(IconSize.sm),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    // 顯示添加照片佔位符
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(IconSize.xl),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )

                        Text(
                            text = "點擊選擇封面照片",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = Spacing.sm)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CoverPhotoSectionEmptyPreview() {
    TravelRecordTheme {
        Surface {
            CoverPhotoSection(
                coverPhotoPath = null,
                onSelectPhoto = {},
                onRemovePhoto = {},
                modifier = Modifier.padding(Spacing.lg)
            )
        }
    }
}
