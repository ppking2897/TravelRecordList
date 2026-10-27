package io.github.ppking2897.travelrecord.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.ItemsByDate
import io.github.ppking2897.travelrecord.core.presentation.theme.CompletedStateColors
import io.github.ppking2897.travelrecord.core.presentation.theme.Spacing
import io.github.ppking2897.travelrecord.core.presentation.theme.TimelineDimensions
import io.github.ppking2897.travelrecord.core.presentation.theme.TravelRecordTheme
import io.github.ppking2897.travelrecord.util.toDateList
import io.github.ppking2897.travelrecord.util.toShortString
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
        color = MaterialTheme.colorScheme.surface
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            // 每格寬度：平分整排比固定寬度寬就撐滿；天數多、排不下時維持固定寬度，可以左右滑。
            // 格與格之間不留空隙，每格的連接線才會接成一條
            val tabCount = nodes.size + 1 // 總覽 + 每一天
            val availableWidth = maxWidth - TimelineDimensions.horizontalPadding * 2
            val itemWidth = maxOf(
                TimelineDimensions.nodeSpacing + TimelineDimensions.nodeSize,
                availableWidth / tabCount
            )

            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = TimelineDimensions.horizontalPadding,
                        vertical = TimelineDimensions.verticalPadding
                    ),
                // Top 對齊：每格各行固定佔位、高度一致，節點那一排才會在同一條水平線上
                verticalAlignment = Alignment.Top
            ) {
                // 「全部」tab
                item {
                    AllTabItem(
                        width = itemWidth,
                        isSelected = selectedDate == null,
                        showTrailingLine = nodes.isNotEmpty(),
                        onClick = { onDateSelected(null) }
                    )
                }

                itemsIndexed(nodes) { index, node ->
                    val isSelected = selectedDate == node.date
                    val isLast = index == nodes.lastIndex

                    TimelineNodeItem(
                        node = node,
                        width = itemWidth,
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
}

/**
 * 「全部」tab 項目
 *
 * 跟每一天的節點同樣的行數與高度：「Day N」那一行留空，「總覽」放在日期那一行。
 */
@Composable
private fun AllTabItem(
    width: Dp,
    isSelected: Boolean,
    showTrailingLine: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(width)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 對應「Day N」那一行：留空但佔高度
        Text(
            text = " ",
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(TimelineDimensions.labelSpacing))

        NodeTrack(showLeadingLine = false, showTrailingLine = showTrailingLine) {
            if (isSelected) SuitcaseNode() else HollowNode()
        }

        Spacer(modifier = Modifier.height(TimelineDimensions.labelSpacing))

        DateLabel(text = "總覽", isSelected = isSelected)

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
            text = "0/0",
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
    width: Dp,
    isSelected: Boolean,
    showLeadingLine: Boolean,
    showTrailingLine: Boolean,
    isToday: Boolean = false,
    onClick: () -> Unit
) {
    // 地點與進度的文字：選中用墨色粗體，全部完成用深綠，其餘用次要文字色
    val detailColor = when {
        isSelected -> MaterialTheme.colorScheme.onSurface
        node.isAllCompleted -> CompletedStateColors.content
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val detailWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

    Column(
        modifier = Modifier
            .width(width)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Day 標籤（固定 style，選中只改粗細與顏色，避免 line height 微差造成節點跳動）
        Text(
            text = "Day ${node.dayNumber}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(TimelineDimensions.labelSpacing))

        NodeTrack(showLeadingLine = showLeadingLine, showTrailingLine = showTrailingLine) {
            // 今天的 halo 外圈（不撐大節點那一排的高度）
            if (isToday && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(TimelineDimensions.itemNodeSize + 6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                )
            }
            when {
                isSelected -> SuitcaseNode()
                node.isAllCompleted -> CompletedNode()
                // 待規劃：空心 + 虛線邊框
                node.totalCount == 0 -> HollowNode(dashed = true)
                else -> HollowNode()
            }
        }

        Spacer(modifier = Modifier.height(TimelineDimensions.labelSpacing))

        DateLabel(text = node.date.toShortString(), isSelected = isSelected)

        // 地點名稱
        Text(
            text = node.primaryLocationName ?: "",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = detailWeight,
            color = detailColor,
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
            text = "${node.completedCount}/${node.totalCount}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = detailWeight,
            color = detailColor,
            textAlign = TextAlign.Center,
            modifier = if (node.totalCount == 0) Modifier.alpha(0f) else Modifier
        )
    }
}

/**
 * 節點那一排：連接線畫在節點後面、穿過每一格的中心，相鄰的格子接起來就是一條線
 */
@Composable
private fun NodeTrack(
    showLeadingLine: Boolean,
    showTrailingLine: Boolean,
    content: @Composable () -> Unit
) {
    val lineColor = MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(TimelineDimensions.nodeTrackHeight)
            .drawBehind {
                val y = size.height / 2
                val strokeWidth = TimelineDimensions.lineWidth.toPx()
                if (showLeadingLine) {
                    drawLine(lineColor, Offset(0f, y), Offset(size.width / 2, y), strokeWidth)
                }
                if (showTrailingLine) {
                    drawLine(lineColor, Offset(size.width / 2, y), Offset(size.width, y), strokeWidth)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * 空心圓點：米白底蓋住後面的連接線，墨色外框；虛線外框代表這天還沒安排
 */
@Composable
private fun HollowNode(dashed: Boolean = false) {
    val fillColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .size(TimelineDimensions.nodeSize)
            .drawBehind {
                val strokeWidth = TimelineDimensions.lineWidth.toPx()
                drawCircle(color = fillColor)
                drawCircle(
                    color = outlineColor,
                    radius = (size.minDimension - strokeWidth) / 2,
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = if (dashed) {
                            PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.5.dp.toPx()))
                        } else {
                            null
                        }
                    )
                )
            }
    )
}

/**
 * 全部完成那天：綠底、墨框、打勾，跟行程卡片時間軸上已完成的圓點一樣
 */
@Composable
private fun CompletedNode() {
    Box(
        modifier = Modifier
            .size(TimelineDimensions.itemNodeSize)
            .clip(CircleShape)
            .background(CompletedStateColors.indicator)
            .border(TimelineDimensions.lineWidth, MaterialTheme.colorScheme.outline, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "已完成",
            modifier = Modifier.size(12.dp),
            tint = CompletedStateColors.onIndicator
        )
    }
}

/**
 * 選中的那一格：珊瑚紅的行李箱，墨色外框、提把與兩個扣環
 */
@Composable
private fun SuitcaseNode() {
    val bodyColor = MaterialTheme.colorScheme.tertiaryContainer
    val outlineColor = MaterialTheme.colorScheme.outline
    val latchColor = MaterialTheme.colorScheme.surface
    Box(
        modifier = Modifier
            .size(width = 30.dp, height = 24.dp)
            .drawBehind {
                val strokeWidth = TimelineDimensions.lineWidth.toPx()
                val bodyRadius = 7.dp.toPx()

                // 提把：畫在箱身上方、超出這個元件的範圍，下半段會被箱身蓋住
                val handleWidth = 10.dp.toPx()
                drawRoundRect(
                    color = outlineColor,
                    topLeft = Offset((size.width - handleWidth) / 2, -6.dp.toPx()),
                    size = Size(handleWidth, 10.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                    style = Stroke(strokeWidth)
                )

                // 箱身
                drawRoundRect(color = bodyColor, cornerRadius = CornerRadius(bodyRadius))
                drawRoundRect(
                    color = outlineColor,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    cornerRadius = CornerRadius(bodyRadius - strokeWidth / 2),
                    style = Stroke(strokeWidth)
                )

                // 兩個扣環
                val latchSize = 6.dp.toPx()
                val latchGap = 4.dp.toPx()
                val latchTop = (size.height - latchSize) / 2
                val firstLatchX = (size.width - latchSize * 2 - latchGap) / 2
                listOf(firstLatchX, firstLatchX + latchSize + latchGap).forEach { x ->
                    val topLeft = Offset(x, latchTop)
                    val latch = Size(latchSize, latchSize)
                    val radius = CornerRadius(2.dp.toPx())
                    drawRoundRect(color = latchColor, topLeft = topLeft, size = latch, cornerRadius = radius)
                    drawRoundRect(
                        color = outlineColor,
                        topLeft = topLeft,
                        size = latch,
                        cornerRadius = radius,
                        style = Stroke(1.5.dp.toPx())
                    )
                }
            }
    )
}

/**
 * 日期（或「總覽」）：選中那天外面一圈珊瑚紅膠囊。
 * 內距不分選中與否都加，切換時寬高才不會變。
 */
@Composable
private fun DateLabel(text: String, isSelected: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else Color.Transparent)
            .padding(horizontal = Spacing.sm, vertical = 1.dp)
    )
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
