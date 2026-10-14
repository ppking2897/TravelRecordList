package io.github.ppking2897.travelrecord.core.presentation.components

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.ItemsByDate
import io.github.ppking2897.travelrecord.core.presentation.theme.Spacing
import io.github.ppking2897.travelrecord.core.presentation.theme.TimelineDimensions
import io.github.ppking2897.travelrecord.core.presentation.theme.TravelRecordTheme
import io.github.ppking2897.travelrecord.util.toDateList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import androidx.compose.ui.tooling.preview.Preview

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
    @OptIn(kotlin.time.ExperimentalTime::class)
    val today = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())

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
            // Top 對齊：選中的節點會因為顯示「地點/今天/進度」變高，若用
            // CenterVertically 中央對齊，圓點會隨 Column 高度浮動導致連接線
            // 斷層；Top 對齊讓圓點在「Day N 標籤」下方的位置固定，保持一直線。
            verticalAlignment = Alignment.Top
        ) {
            // 「全部」tab
            item {
                AllTabItem(
                    isSelected = selectedDate == null,
                    onClick = { onDateSelected(null) }
                )
            }

            itemsIndexed(nodes) { index, node ->
                val isSelected = selectedDate == node.date
                val isFirst = index == 0
                val isLast = index == nodes.lastIndex

                TimelineNodeItem(
                    node = node,
                    isSelected = isSelected,
                    showLeadingLine = true,
                    showTrailingLine = !isLast,
                    isToday = node.date == today,
                    onClick = { onDateSelected(node.date) }
                )
            }
        }
    }
}

/**
 * 「全部」tab 項目
 */
@Composable
private fun AllTabItem(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val nodeSize by animateDpAsState(
        targetValue = if (isSelected) TimelineDimensions.nodeSelectedSize else TimelineDimensions.nodeSize,
        animationSpec = tween(durationMillis = 200)
    )

    val nodeColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .width(TimelineDimensions.nodeSpacing + TimelineDimensions.nodeSize)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "總覽",
            // 固定 style 避免字型 line height 微差造成 Column 高度跳動；
            // 選中改用 Bold 區分。
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(TimelineDimensions.labelSpacing))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // 同 TimelineNodeItem 一致的固定 container size，確保圓點水平線對齊
            Box(
                modifier = Modifier.size(TimelineDimensions.nodeSelectedSize + 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(nodeSize)
                        .clip(CircleShape)
                        .background(nodeColor)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(TimelineDimensions.lineWidth)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }

        Spacer(modifier = Modifier.height(TimelineDimensions.labelSpacing))

        // 與 TimelineNodeItem 對齊的隱形 placeholder：對應下段三行（地點/今天/進度），
        // 全部 alpha=0 但佔空間，確保 AllTab 和 Day 節點 Column 高度永遠一致。
        Text(
            text = "-",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(horizontal = Spacing.xs)
                .alpha(0f)
        )
        Text(
            text = "今天",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.alpha(0f)
        )
        Text(
            text = "(0/0)",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.alpha(0f)
        )
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
    isToday: Boolean = false,
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
        // Day 標籤（固定 style + Bold 區分選中，避免 labelSmall↔labelMedium 切換
        // 在不同字型 fallback 下 line height 微差造成節點跳動）
        Text(
            text = "Day ${node.dayNumber}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
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

            // 節點容器：固定 max size（nodeSelectedSize + 4dp 給 today halo 用），
            // 不管 isToday / isSelected 組合都維持同高，連接線才能對齊。
            Box(
                modifier = Modifier.size(TimelineDimensions.nodeSelectedSize + 4.dp),
                contentAlignment = Alignment.Center
            ) {
                // 今天的 halo 外圈（固定占位，不撐大容器）
                if (isToday) {
                    Box(
                        modifier = Modifier
                            .size(nodeSize + 4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    )
                }
                // 節點圓點本體
                if (node.totalCount == 0) {
                    // 待規劃：空心 + 虛線邊框（不改變 Box size，不影響對齊）
                    val outlineColor = nodeColor
                    Box(
                        modifier = Modifier
                            .size(nodeSize)
                            .drawBehind {
                                drawCircle(
                                    color = outlineColor,
                                    style = Stroke(
                                        width = 1.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(3.dp.toPx(), 2.5.dp.toPx())
                                        )
                                    )
                                )
                            }
                    )
                } else {
                    // 實心圓（已安排，未完成 / 已完成 / 選中 各用不同 nodeColor）
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

        // 今天標示：非今天時用 alpha 0 保留空間，所有節點下段行數一致
        Text(
            text = "今天",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = if (isToday) Modifier else Modifier.alpha(0f)
        )

        // 進度指示 (如 2/3)：0/0 時用 alpha 0 隱藏文字但保留高度，
        // 讓所有節點的垂直尺寸一致（不會因沒項目而變矮）
        Text(
            text = "(${node.completedCount}/${node.totalCount})",
            style = MaterialTheme.typography.labelSmall,
            color = if (node.isAllCompleted) successColor else textColor.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = if (node.totalCount == 0) Modifier.alpha(0f) else Modifier
        )
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
