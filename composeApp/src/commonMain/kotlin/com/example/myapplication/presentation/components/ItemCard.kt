package com.example.myapplication.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import com.example.myapplication.domain.entity.ItineraryItem
import com.example.myapplication.domain.entity.Photo
import com.example.myapplication.presentation.theme.AppGradients
import com.example.myapplication.presentation.theme.CardStyle
import com.example.myapplication.presentation.theme.CompletedStateColors
import com.example.myapplication.presentation.theme.ComponentSize
import com.example.myapplication.presentation.theme.CornerRadius
import com.example.myapplication.presentation.theme.IconSize
import com.example.myapplication.presentation.theme.Spacing
import com.example.myapplication.presentation.theme.TimelineDimensions

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
    onDeletePhoto: ((String) -> Unit)? = null,
    // 批量選擇相關
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectionToggle: () -> Unit = {}
) {
    val expanded = isExpanded
    var showMoreMenu by remember { mutableStateOf(false) }
    val isLightTheme = !isSystemInDarkTheme()

    // 動畫顏色 - 使用 CompletedStateColors
    val cardBackgroundColor by animateColorAsState(
        targetValue = if (item.isCompleted) {
            if (isLightTheme) CompletedStateColors.lightBackground
            else CompletedStateColors.darkBackground
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "cardBackground"
    )

    val cardBorderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else if (item.isCompleted) {
        if (isLightTheme) CompletedStateColors.lightBorder else CompletedStateColors.darkBorder
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // ========== 選擇模式時顯示 Checkbox ==========
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectionToggle() },
                modifier = Modifier.padding(top = Spacing.sm),
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // ========== 左側時間軸區域 ==========
        if (!isSelectionMode) {
            TimelineSection(
                arrivalTime = item.arrivalTime,
                departureTime = item.departureTime,
                isCompleted = item.isCompleted,
                isLastItem = isLastItem
            )

            Spacer(modifier = Modifier.width(Spacing.md))
        }

        // ========== 右側內容卡片 ==========
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = if (isSelectionMode) true else onExpandToggle != null) {
                    if (isSelectionMode) {
                        onSelectionToggle()
                    } else {
                        onExpandToggle?.invoke(item.id)
                    }
                },
            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (item.isCompleted) CardStyle.listCardElevation / 2
                    else CardStyle.listCardElevation
            ),
            shape = RoundedCornerShape(CornerRadius.md),
            border = if (isSelected) BorderStroke(2.dp, cardBorderColor)
                else if (item.isCompleted) BorderStroke(TimelineDimensions.lineWidth / 2, cardBorderColor)
                else null
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
 * 使用 TimelineDimensions 統一尺寸，使用 CompletedStateColors 區分完成狀態
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
    val isLightTheme = !isSystemInDarkTheme()

    // 使用 CompletedStateColors 區分完成狀態
    val timelineColor = if (isCompleted) {
        if (isLightTheme) CompletedStateColors.lightIcon else CompletedStateColors.darkIcon
    } else {
        MaterialTheme.colorScheme.primary
    }

    val timeTextColor = if (isCompleted) {
        if (isLightTheme) CompletedStateColors.lightText.copy(alpha = 0.6f)
        else CompletedStateColors.darkText.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier.width(TimelineDimensions.nodeSpacing + TimelineDimensions.horizontalPadding / 2),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 時間顯示
        if (primaryTime != null) {
            Text(
                text = formatTime(primaryTime),
                style = MaterialTheme.typography.labelLarge,
                color = timeTextColor
            )

            Spacer(modifier = Modifier.height(TimelineDimensions.labelSpacing))
        }

        // 時間軸圓點 - 使用 TimelineDimensions.nodeSize
        Box(
            modifier = Modifier
                .size(TimelineDimensions.nodeSize)
                .background(timelineColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(TimelineDimensions.nodeSize - Spacing.xs),
                    tint = if (isLightTheme) CompletedStateColors.lightBackground
                    else CompletedStateColors.darkBackground
                )
            }
        }

        // 時間軸連接線 - 使用 TimelineDimensions.lineWidth
        if (!isLastItem) {
            Box(
                modifier = Modifier
                    .width(TimelineDimensions.lineWidth)
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
 * 使用 CompletedStateColors 定義的完成狀態色彩
 */
@Composable
private fun CompletedBadge(modifier: Modifier = Modifier) {
    val isLightTheme = !isSystemInDarkTheme()
    val backgroundColor = if (isLightTheme) CompletedStateColors.lightBackground
        else CompletedStateColors.darkBackground
    val contentColor = if (isLightTheme) CompletedStateColors.lightContent
        else CompletedStateColors.darkContent

    Surface(
        modifier = modifier,
        color = backgroundColor,
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
                tint = contentColor
            )
            Text(
                text = "已完成",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

/**
 * 封面照片顯示
 * 使用 AppGradients.cardOverlayGradient 作為底部遮罩
 */
@Composable
fun CoverPhotoDisplay(
    photo: Photo,
    modifier: Modifier = Modifier,
    showOverlay: Boolean = false,
    overlayContent: @Composable BoxScope.() -> Unit = {}
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(CornerRadius.sm)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LocalImage(
                photo = photo,
                contentDescription = "封面照片",
                modifier = Modifier.fillMaxSize(),
                useThumbnail = true
            )

            // 底部漸層遮罩 - 使用 AppGradients.cardOverlayGradient
            if (showOverlay) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                        .align(Alignment.BottomCenter)
                        .background(AppGradients.cardOverlayGradient)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(Spacing.sm),
                    content = overlayContent
                )
            }
        }
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
