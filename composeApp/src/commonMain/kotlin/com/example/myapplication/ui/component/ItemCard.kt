package com.example.myapplication.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.ItineraryItem

@Composable
fun ItemCard(
    item: ItineraryItem,
    onToggleComplete: (String) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onExpandToggle: ((String) -> Unit)? = null,
    onAddPhoto: ((String) -> Unit)? = null,
    onPhotoClick: ((String) -> Unit)? = null,
    onSetCoverPhoto: ((String, String) -> Unit)? = null,
    onDeletePhoto: ((String) -> Unit)? = null
) {
    var expanded by remember(isExpanded) { mutableStateOf(isExpanded) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 標題列：Checkbox + 活動名稱 + 編輯/刪除按鈕
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isCompleted,
                        onCheckedChange = { onToggleComplete(item.id) }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = item.activity,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = item.location.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // 顯示時間資訊
                        val timeText = when {
                            item.arrivalTime != null && item.departureTime != null -> 
                                "${item.arrivalTime} - ${item.departureTime}"
                            item.arrivalTime != null -> 
                                "到達 ${item.arrivalTime}"
                            item.departureTime != null -> 
                                "離開 ${item.departureTime}"
                            else -> null
                        }
                        timeText?.let { text ->
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Row {
                    IconButton(onClick = { onEdit(item.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "編輯")
                    }
                    IconButton(onClick = { onDelete(item.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "刪除")
                    }
                }
            }
            
            // 封面照片（如果有）
            item.getCoverPhoto()?.let { coverPhoto ->
                CoverPhotoDisplay(
                    photoPath = coverPhoto.filePath,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
            
            // 標籤顯示
            if (item.hashtags.isNotEmpty()) {
                HashtagRow(
                    hashtags = item.hashtags,
                    onHashtagClick = { /* 可選：點擊標籤進行篩選 */ }
                )
            }
            
            // 簡短備註（未展開時）
            if (!expanded && item.notes.isNotBlank()) {
                Text(
                    text = item.notes.take(50) + if (item.notes.length > 50) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 展開/收起按鈕
            if (onExpandToggle != null) {
                TextButton(
                    onClick = { 
                        expanded = !expanded
                        onExpandToggle(item.id)
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (expanded) "收起" else "展開")
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // 展開的詳細內容
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                ExpandedItemDetail(
                    item = item,
                    onAddPhoto = onAddPhoto,
                    onPhotoClick = onPhotoClick,
                    onSetCoverPhoto = onSetCoverPhoto,
                    onDeletePhoto = onDeletePhoto
                )
            }
        }
    }
}

@Composable
fun CoverPhotoDisplay(
    photoPath: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // TODO: 實際載入圖片
            // 暫時顯示佔位符
            Icon(
                Icons.Default.Image,
                contentDescription = "封面照片",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}


