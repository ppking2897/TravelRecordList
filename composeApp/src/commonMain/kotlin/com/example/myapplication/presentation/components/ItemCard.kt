package com.example.myapplication.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.example.myapplication.domain.entity.ItineraryItem
import com.example.myapplication.domain.entity.Photo
import com.example.myapplication.presentation.theme.CardStyle
import com.example.myapplication.presentation.theme.ComponentSize
import com.example.myapplication.presentation.theme.CornerRadius
import com.example.myapplication.presentation.theme.IconSize
import com.example.myapplication.presentation.theme.Spacing

/**
 * 時間軸風格的行程項目卡片
 *
 * 設計特點：
 * - 左側時間軸顯示到達/離開時間
 * - 右側主內容區展示活動資訊
 * - 完成狀態有明顯的視覺回饋
 * - 操作按鈕收斂在 more menu 中
 */
@Composable
fun ItemCard(
    item: ItineraryItem,
    onToggleComplete: (String) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    isLastItem: Boolean = false,
    onExpandToggle: ((String) -> Unit)? = null,
    onAddPhoto: ((String) -> Unit)? = null,
    onSetCoverPhoto: ((String, String) -> Unit)? = null,
    onDeletePhoto: ((String) -> Unit)? = null
) {
    val expanded = isExpanded
    var showMoreMenu by remember { mutableStateOf(false) }

    // 動畫顏色
    val cardBackgroundColor by animateColorAsState(
        targetValue = if (item.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "cardBackground"
    )

    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        // ========== 左側時間軸區域 ==========
        TimelineSection(
            arrivalTime = item.arrivalTime,
            departureTime = item.departureTime,
            isCompleted = item.isCompleted,
            isLastItem = isLastItem
        )

        Spacer(modifier = Modifier.width(Spacing.md))

        // ========== 右側內容卡片 ==========
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = onExpandToggle != null) {
                    onExpandToggle?.invoke(item.id)
                },
            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (item.isCompleted) CardStyle.listCardElevation / 2 else CardStyle.listCardElevation
            ),
            shape = RoundedCornerShape(CornerRadius.md)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md)
            ) {
                // 頂部：封面照片（如果有且未展開）
                if (!expanded) {
                    item.getCoverPhoto()?.let { coverPhoto ->
                        CoverPhotoDisplay(
                            photo = coverPhoto,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ComponentSize.thumbnailSize)
                                .clip(RoundedCornerShape(CornerRadius.sm))
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                    }
                }

                // 標題列：活動名稱 + more 按鈕
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // 活動名稱
                        Text(
                            text = item.activity,
                            style = MaterialTheme.typography.titleMedium,
                            textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null,
                            color = if (item.isCompleted) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        // 地點標籤
                        LocationChip(location = item.location.name)
                    }

                    // More 按鈕
                    Box {
                        IconButton(
                            onClick = { showMoreMenu = true },
                            modifier = Modifier.size(IconSize.lg)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "更多選項",
                                modifier = Modifier.size(IconSize.sm),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (item.isCompleted) "標記未完成" else "標記完成") },
                                onClick = {
                                    showMoreMenu = false
                                    onToggleComplete(item.id)
                                },
                                leadingIcon = {
                                    Icon(
                                        if (item.isCompleted) Icons.Default.Refresh else Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("編輯") },
                                onClick = {
                                    showMoreMenu = false
                                    onEdit(item.id)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "刪除",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    onDelete(item.id)
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

                // 標籤列（Hashtags）
                if (item.hashtags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    HashtagRow(
                        hashtags = item.hashtags,
                        onHashtagClick = { /* 篩選功能 */ }
                    )
                }

                // 備註預覽（未展開時）
                if (!expanded && item.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 完成狀態標籤
                if (item.isCompleted) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    CompletedBadge()
                }

                // 展開指示器
                if (onExpandToggle != null) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "收起" else "展開",
                            modifier = Modifier.size(IconSize.sm),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
                        onSetCoverPhoto = onSetCoverPhoto,
                        onDeletePhoto = onDeletePhoto
                    )
                }
            }
        }
    }
}

/**
 * 時間軸區域
 */
@Composable
private fun TimelineSection(
    arrivalTime: kotlinx.datetime.LocalTime?,
    departureTime: kotlinx.datetime.LocalTime?,
    isCompleted: Boolean,
    isLastItem: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryTime = arrivalTime ?: departureTime
    val timelineColor = if (isCompleted) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier.width(Spacing.xxxl + Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 時間顯示
        if (primaryTime != null) {
            Text(
                text = formatTime(primaryTime),
                style = MaterialTheme.typography.labelLarge,
                color = if (isCompleted) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )

            Spacer(modifier = Modifier.height(Spacing.xs))
        }

        // 時間軸圓點
        Box(
            modifier = Modifier
                .size(IconSize.xs)
                .background(timelineColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(IconSize.xs - Spacing.xs),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // 時間軸連接線
        if (!isLastItem) {
            Box(
                modifier = Modifier
                    .width(Spacing.xs / 2)
                    .weight(1f)
                    .background(timelineColor.copy(alpha = 0.3f))
            )
        }
    }
}

/**
 * 地點標籤
 */
@Composable
private fun LocationChip(
    location: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(CornerRadius.xs)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Icon(
                Icons.Default.Place,
                contentDescription = null,
                modifier = Modifier.size(IconSize.xs),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = location,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 完成狀態標籤
 */
@Composable
private fun CompletedBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(CornerRadius.xs)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(IconSize.xs),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "已完成",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 封面照片顯示
 */
@Composable
fun CoverPhotoDisplay(
    photo: Photo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(CornerRadius.sm)
    ) {
        LocalImage(
            photo = photo,
            contentDescription = "封面照片",
            modifier = Modifier.fillMaxSize(),
            useThumbnail = true
        )
    }
}

/**
 * 格式化時間顯示
 */
private fun formatTime(time: kotlinx.datetime.LocalTime): String {
    val hour = time.hour.toString().padStart(2, '0')
    val minute = time.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}
