package com.example.myapplication.core.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myapplication.core.presentation.theme.CardStyle
import com.example.myapplication.core.presentation.theme.Spacing

/**
 * 批量操作工具列
 *
 * 在選擇模式下顯示於畫面底部，提供批量刪除和標記完成功能
 */
@Composable
fun BatchActionBar(
    selectedCount: Int,
    onDelete: () -> Unit,
    onMarkComplete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = CardStyle.detailCardElevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左側：取消 + 數量
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCancel) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "取消選擇",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "已選擇 $selectedCount 項",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 右側：操作按鈕（IconButton 更緊湊）
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onMarkComplete,
                    enabled = selectedCount > 0
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "標記完成",
                        tint = if (selectedCount > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
                IconButton(
                    onClick = onDelete,
                    enabled = selectedCount > 0
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "刪除",
                        tint = if (selectedCount > 0) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }
        }
    }
}

/**
 * 帶動畫效果的批量操作工具列
 */
@Composable
fun AnimatedBatchActionBar(
    visible: Boolean,
    selectedCount: Int,
    onDelete: () -> Unit,
    onMarkComplete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        BatchActionBar(
            selectedCount = selectedCount,
            onDelete = onDelete,
            onMarkComplete = onMarkComplete,
            onCancel = onCancel
        )
    }
}
