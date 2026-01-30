package com.example.myapplication.presentation.components

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
import com.example.myapplication.domain.usecase.ItemsByDate
import com.example.myapplication.presentation.theme.Spacing
import com.example.myapplication.presentation.theme.TimelineDimensions
import com.example.myapplication.presentation.theme.TravelRecordTheme
import com.example.myapplication.util.toDateList
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 時間軸導覽節點資料
 *
 * @property dayNumber 第幾天 (Day 1, Day 2, ...)
 * @property date 日期
 * @property primaryLocationName 該日主要地點名稱
 */
data class TimelineNode(
    val dayNumber: Int,
    val date: LocalDate,
    val primaryLocationName: String?
)

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
            val primaryLocation = itemsForDate?.items?.firstOrNull()?.location?.name
            TimelineNode(
                dayNumber = index + 1,
                date = date,
                primaryLocationName = primaryLocation
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

            // 節點圓點
            Box(
                modifier = Modifier
                    .size(nodeSize)
                    .clip(CircleShape)
                    .background(nodeColor)
            )

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
