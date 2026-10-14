package io.github.ppking2897.travelrecord.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Photo
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.rememberCoilZoomState

/**
 * 照片預覽 Dialog
 * 支援：
 * - 全螢幕預覽
 * - 縮放手勢
 * - 設為封面
 * - 刪除照片
 */
@Composable
fun PhotoPreviewDialog(
    photo: Photo,
    isCover: Boolean,
    onDismiss: () -> Unit,
    onSetCoverPhoto: (() -> Unit)? = null,
    onDeletePhoto: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val zoomState = rememberCoilZoomState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            // 照片顯示區 - 使用 ZoomImage
            CoilZoomAsyncImage(
                model = photo.filePath,
                contentDescription = "照片預覽",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                zoomState = zoomState,
                onTap = { onDismiss() }
            )

            // 頂部操作列
            TopActionBar(
                isCover = isCover,
                onClose = onDismiss,
                onSetCoverPhoto = onSetCoverPhoto,
                onDeletePhoto = { showDeleteConfirm = true },
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // 底部資訊
            PhotoInfoBar(
                photo = photo,
                isCover = isCover,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // 刪除確認 Dialog
    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            title = "刪除照片",
            message = "確定要刪除這張照片嗎？此操作無法復原。",
            onConfirm = {
                showDeleteConfirm = false
                onDeletePhoto?.invoke()
                onDismiss()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun TopActionBar(
    isCover: Boolean,
    onClose: () -> Unit,
    onSetCoverPhoto: (() -> Unit)?,
    onDeletePhoto: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 關閉按鈕
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "關閉",
                    tint = Color.White
                )
            }

            // 操作按鈕
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 設為封面
                if (onSetCoverPhoto != null && !isCover) {
                    IconButton(onClick = onSetCoverPhoto) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "設為封面",
                            tint = Color.White
                        )
                    }
                }

                // 刪除
                if (onDeletePhoto != null) {
                    IconButton(onClick = onDeletePhoto) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "刪除",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoInfoBar(
    photo: Photo,
    isCover: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 照片資訊
            Column {
                if (isCover) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "封面照片",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 檔案大小
                val sizeText = when {
                    photo.fileSize >= 1024 * 1024 -> {
                        val mb = photo.fileSize / (1024f * 1024f)
                        "${(mb * 10).toInt() / 10f} MB"
                    }
                    photo.fileSize >= 1024 -> {
                        val kb = photo.fileSize / 1024f
                        "${(kb * 10).toInt() / 10f} KB"
                    }
                    else -> "${photo.fileSize} B"
                }
                Text(
                    text = sizeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // 尺寸資訊
            if (photo.width != null && photo.height != null) {
                Text(
                    text = "${photo.width} × ${photo.height}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 簡化版照片預覽 - 僅用於編輯畫面 (只有刪除功能)
 */
@Composable
fun SimplePhotoPreviewDialog(
    filePath: String,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val zoomState = rememberCoilZoomState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            // 照片 - 使用 ZoomImage
            CoilZoomAsyncImage(
                model = filePath,
                contentDescription = "照片預覽",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                zoomState = zoomState,
                onTap = { onDismiss() }
            )

            // 頂部操作列
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "關閉",
                            tint = Color.White
                        )
                    }

                    if (onDelete != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "刪除",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            title = "移除照片",
            message = "確定要移除這張照片嗎？",
            onConfirm = {
                showDeleteConfirm = false
                onDelete?.invoke()
                onDismiss()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
