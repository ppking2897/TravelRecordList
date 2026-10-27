package io.github.ppking2897.travelrecord.core.presentation.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ppking2897.travelrecord.core.presentation.theme.DateColorPalette
import io.github.ppking2897.travelrecord.core.presentation.util.DateTimeFormatter
import io.github.ppking2897.travelrecord.feature.route.domain.model.MapMarker

/**
 * Android 地圖視圖
 *
 * Phase 1 簡化版本：使用列表顯示地點，可開啟 Google Maps
 * Phase 2：按日期分組顯示，每日配一組顏色（DateColorPalette）
 * TODO: Phase 3 以後實作 MapLibre 真實地圖 + 同日虛線 Polyline
 */
@Composable
actual fun ItineraryMapView(
    markers: List<MapMarker>,
    selectedMarker: MapMarker?,
    onMarkerClick: (MapMarker) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current

    if (markers.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "沒有可顯示的地點",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val groupedByDate = markers.groupBy { it.date }.toSortedMap()
    val sortedDates = groupedByDate.keys.toList()

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "點擊地點查看詳情，或開啟 Google Maps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sortedDates.forEachIndexed { dayIndex, date ->
                val dayColor = DateColorPalette.colorForDayIndex(dayIndex)
                val dateMarkers = groupedByDate.getValue(date).sortedBy { it.order }

                item(key = "header_$date") {
                    DayGroupHeader(
                        dayIndex = dayIndex + 1,
                        date = DateTimeFormatter.formatDate(date),
                        color = dayColor,
                        count = dateMarkers.size
                    )
                }

                items(dateMarkers, key = { it.id }) { marker ->
                    MarkerListItem(
                        marker = marker,
                        isSelected = marker == selectedMarker,
                        dayColor = dayColor,
                        onClick = { onMarkerClick(marker) },
                        onOpenInMaps = {
                            val encodedName = Uri.encode(marker.locationName)
                            val uri = Uri.parse("geo:0,0?q=$encodedName")
                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                val webUri = Uri.parse(
                                    "https://www.google.com/maps/search/?api=1&query=$encodedName"
                                )
                                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayGroupHeader(
    dayIndex: Int,
    date: String,
    color: Color,
    count: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "第 $dayIndex 天 · $date",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$count 個地點",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MarkerListItem(
    marker: MapMarker,
    isSelected: Boolean,
    dayColor: Color,
    onClick: () -> Unit,
    onOpenInMaps: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(dayColor),
                contentAlignment = Alignment.Center
            ) {
                if (marker.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "已完成",
                        tint = contentColorOn(dayColor),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "${marker.order}",
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColorOn(dayColor)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = marker.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = marker.locationName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onOpenInMaps) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "在 Google Maps 開啟",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 根據背景亮度選擇黑 / 白文字以確保對比。
 */
private fun contentColorOn(background: Color): Color =
    if (background.luminance() > 0.5f) Color.Black else Color.White
