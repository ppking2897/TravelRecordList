package com.example.myapplication.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.entity.ItineraryItem

/**
 * 展開的行程項目詳細內容
 *
 * 照片點擊會自動開啟預覽，可在預覽中刪除或設為封面
 */
@Composable
fun ExpandedItemDetail(
    item: ItineraryItem,
    onAddPhoto: ((String) -> Unit)?,
    onSetCoverPhoto: ((String, String) -> Unit)?,
    onDeletePhoto: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider()
        
        // 完整描述
        if (item.notes.isNotBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "備註",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = item.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // 時間資訊
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "時間資訊",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            item.arrivalTime?.let { arrivalTime ->
                Text(
                    text = "到達時間：$arrivalTime",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            item.departureTime?.let { departureTime ->
                Text(
                    text = "離開時間：$departureTime",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            item.stayDuration()?.let { duration ->
                Text(
                    text = "停留時間：${duration.inWholeHours}小時${duration.inWholeMinutes % 60}分鐘",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 照片集
        if (item.photos.isNotEmpty() || onAddPhoto != null) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "照片 (${item.photos.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    onAddPhoto?.let { callback ->
                        TextButton(
                            onClick = { callback(item.id) }
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("新增照片")
                        }
                    }
                }
                
                if (item.photos.isNotEmpty()) {
                    // 計算高度：每行 3 張，假設寬度足夠，高度約為寬度的 1/3 (因為 aspect ratio 1f)
                    // 這裡簡單給一個高度限制，避免巢狀捲動錯誤
                    val rows = (item.photos.size + 2) / 3
                    val height = (rows * 120).dp // 粗略估計

                    PhotoGallery(
                        photos = item.photos,
                        coverPhotoId = item.coverPhotoId,
                        onSetCoverPhoto = { photoId ->
                            onSetCoverPhoto?.invoke(item.id, photoId)
                        },
                        onDeletePhoto = onDeletePhoto,
                        modifier = Modifier.height(height),
                        enablePreview = true
                    )
                }
            }
        }
    }
}
