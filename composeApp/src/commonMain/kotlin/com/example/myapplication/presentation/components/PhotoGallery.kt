package com.example.myapplication.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.entity.Photo

/**
 * 照片網格元件 (含預覽功能)
 *
 * @param photos 照片列表
 * @param coverPhotoId 封面照片 ID
 * @param onSetCoverPhoto 設為封面回調 (photoId)
 * @param onDeletePhoto 刪除照片回調 (photoId)
 * @param enablePreview 是否啟用點擊預覽 (預設 true)
 */
@Composable
fun PhotoGallery(
    photos: List<Photo>,
    coverPhotoId: String?,
    onSetCoverPhoto: ((String) -> Unit)?,
    onDeletePhoto: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
    enablePreview: Boolean = true
) {
    var selectedPhotoForPreview by remember { mutableStateOf<Photo?>(null) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(photos, key = { it.id }) { photo ->
            PhotoGridItem(
                photo = photo,
                isCover = photo.id == coverPhotoId,
                onClick = {
                    if (enablePreview) {
                        selectedPhotoForPreview = photo
                    }
                },
                onSetCoverPhoto = { onSetCoverPhoto?.invoke(photo.id) },
                onDelete = { onDeletePhoto?.invoke(photo.id) }
            )
        }
    }

    // 照片預覽 Dialog
    selectedPhotoForPreview?.let { photo ->
        PhotoPreviewDialog(
            photo = photo,
            isCover = photo.id == coverPhotoId,
            onDismiss = { selectedPhotoForPreview = null },
            onSetCoverPhoto = onSetCoverPhoto?.let { callback ->
                {
                    callback(photo.id)
                    selectedPhotoForPreview = null
                }
            },
            onDeletePhoto = onDeletePhoto?.let { callback ->
                {
                    callback(photo.id)
                    selectedPhotoForPreview = null
                }
            }
        )
    }
}

/**
 * 照片網格元件 (舊版相容 - 使用 onPhotoClick)
 */
@Composable
fun PhotoGallery(
    photos: List<Photo>,
    coverPhotoId: String?,
    onPhotoClick: ((String) -> Unit)?,
    onSetCoverPhoto: ((String) -> Unit)?,
    onDeletePhoto: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(photos, key = { it.id }) { photo ->
            PhotoGridItem(
                photo = photo,
                isCover = photo.id == coverPhotoId,
                onClick = { onPhotoClick?.invoke(photo.id) },
                onSetCoverPhoto = { onSetCoverPhoto?.invoke(photo.id) },
                onDelete = { onDeletePhoto?.invoke(photo.id) }
            )
        }
    }
}

/**
 * 單個照片網格項目
 */
@Composable
fun PhotoGridItem(
    photo: Photo,
    isCover: Boolean,
    onClick: () -> Unit,
    onSetCoverPhoto: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 使用縮圖顯示照片
            LocalImage(
                photo = photo,
                contentDescription = "照片",
                modifier = Modifier.fillMaxSize(),
                useThumbnail = true
            )
            
            // 封面標記
            if (isCover) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "封面",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "封面",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            // 操作選單按鈕
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "選項",
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (!isCover) {
                        DropdownMenuItem(
                            text = { Text("設為封面") },
                            onClick = {
                                showMenu = false
                                onSetCoverPhoto()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Star, contentDescription = null)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("刪除") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}
