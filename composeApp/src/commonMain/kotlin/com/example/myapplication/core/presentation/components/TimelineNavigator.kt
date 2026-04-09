package com.example.myapplication.core.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.feature.itinerary_item.domain.usecase.ItemsByDate
import com.example.myapplication.core.presentation.theme.Spacing
import com.example.myapplication.core.presentation.theme.TimelineDimensions
import com.example.myapplication.core.presentation.theme.TravelRecordTheme
import com.example.myapplication.util.toDateList
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 時間軸導覽節點資料
 *
 * @property dayNumber 第幾天 (Day 1, Day 2, ...)
 * @property date 日期
 * @property primaryLocationName 該日主要地點名稱
 * @property completedCount 已完成項目數量
 * @property totalCount 總項目數量
 */
data class TimelineNode(
    val dayNumber: Int,
    val date: LocalDate,
    val primaryLocationName: String?,
    val completedCount: Int = 0,
    val totalCount: Int = 0
) {
    /** 是否全部完成 */
    val isAllCompleted: Boolean
        get() = totalCount > 0 && completedCount >= totalCount
}

/**
 * 時間軸水平導覽元件
 *
 * 用於 ItineraryDetailScreen 頂部，顯示行程的日期時間軸
 * 支援水平滑動、點擊跳轉、當前日期高亮
 *
 * @param dateRange 行程日期範圍
 * @param groupedItems 按日期分組的行程項目
 * @param selectedDate 當前選擇的日期
 * @param onDateSelected 日期選擇回調
 * @param modifier Modifier
 */
@Composable
fun TimelineNavigator(
    dateRange: ClosedRange<LocalDate>,
    groupedItems: List<ItemsByDate>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    onQuickAdd: (afterDayIndex: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dates = dateRange.toDateList()
    val listState = rememberLazyListState()

    // 建立時間軸節點資料
    val nodes = remember(dates, groupedItems) {
        dates.mapIndexed { index, date ->
            val itemsForDate = groupedItems.find { it.date == date }
            val items = itemsForDate?.items ?: emptyList()
            val primaryLocation = items.firstOrNull()?.location?.name
            TimelineNode(
                dayNumber = index + 1,
                date = date,
                primaryLocationName = primaryLocation,
                completedCount = items.count { it.isCompleted },
                totalCount = items.size
            )
        }
    }

    // 當選中日期變更時，自動滾動到該位置
    LaunchedEffect(selectedDate) {
        selectedDate?.let { date ->
            val index = dates.indexOf(date)
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = TimelineDimensions.horizontalPadding,
                    vertical = TimelineDimensions.verticalPadding
                ),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(nodes) { index, node ->
                val isSelected = selectedDate == node.date
                val isFirst = index == 0
                val isLast = index == nodes.lastIndex

                // 在非第一個節點前顯示快速新增按鈕
                if (!isFirst) {
                    QuickAddButton(
                        onClick = { onQuickAdd(index - 1) }
                    )
                }

                TimelineNodeItem(
                    node = node,
                    isSelected = isSelected,
                    showLeadingLine = !isFirst,
                    showTrailingLine = !isLast,
                    onClick = { onDateSelected(node.date) }
                )
            }
        }
    }
}

/**
 * 快速新增按鈕
 *
 * 顯示在日期節點之間的「+」按鈕
 *
 * @param onClick 點擊回調
 */
@Composable
private fun QuickAddButton(
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(TimelineDimensions.nodeSize)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "快速新增項目",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(TimelineDimensions.nodeSize - 4.dp)
        )
    }
}

/**
 * 時間軸節點項目
 *
 * @param node 節點資料
 * @param isSelected 是否被選中
 * @param showLeadingLine 是否顯示前導連接線
 * @param showTrailingLine 是否顯示後續連接線
 * @param onClick 點擊回調
 */
@Composable
private fun TimelineNodeItem(
    node: TimelineNode,
    isSelected: Boolean,
    showLeadingLine: Boolean,
    showTrailingLine: Boolean,
    onClick: () -> Unit
) {
    val nodeSize by animateDpAsState(
        targetValue = if (isSelected) TimelineDimensions.nodeSelectedSize else TimelineDimensions.nodeSize,
        animationSpec = tween(durationMillis = 200)
    )

    // 完成狀態顏色（綠色）
    val successColor = MaterialTheme.colorScheme.tertiary

    // 根據選中和完成狀態決定節點顏色
    val nodeColor = when {
        node.isAllCompleted -> successColor
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    // 根據選中和完成狀態決定文字顏色
    val textColor = when {
        node.isAllCompleted -> successColor
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .width(TimelineDimensions.nodeSpacing + TimelineDimensions.nodeSize)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Day 標籤
        Text(
            text = "Day ${node.dayNumber}",
            style = if (isSelected) {
                MaterialTheme.typography.labelMedium
            } else {
                MaterialTheme.typography.labelSmall
            },
            color = textColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(TimelineDimensions.labelSpacing))

        // 節點和連接線
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 前導連接線
            if (showLeadingLine) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(TimelineDimensions.lineWidth)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // 節點圓點（完成時顯示打勾圖示）
            Box(
                modifier = Modifier
                    .size(nodeSize)
                    .clip(CircleShape)
                    .background(nodeColor),
                contentAlignment = Alignment.Center
            ) {
                if (node.isAllCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "已完成",
                        modifier = Modifier.size(nodeSize * 0.6f),
                        tint = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }

            // 後續連接線
            if (showTrailingLine) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(TimelineDimensions.lineWidth)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(TimelineDimensions.labelSpacing))

        // 地點名稱
        Text(
            text = node.primaryLocationName ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = Spacing.xs)
        )

        // 進度指示 (如 2/3)
        if (node.totalCount > 0) {
            Text(
                text = "(${node.completedCount}/${node.totalCount})",
                style = MaterialTheme.typography.labelSmall,
                color = if (node.isAllCompleted) successColor else textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// Preview
@Preview
@Composable
private fun TimelineNavigatorPreview() {
    TravelRecordTheme {
        Surface {
            TimelineNavigator(
                dateRange = LocalDate(2024, 3, 15)..LocalDate(2024, 3, 19),
                groupedItems = listOf(
                    ItemsByDate(LocalDate(2024, 3, 15), emptyList()),
                    ItemsByDate(LocalDate(2024, 3, 16), emptyList()),
                    ItemsByDate(LocalDate(2024, 3, 17), emptyList())
                ),
                selectedDate = LocalDate(2024, 3, 16),
                onDateSelected = {}
            )
        }
    }
}
