package io.github.ppking2897.travelrecord.core.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.ppking2897.travelrecord.feature.route.domain.model.MapMarker

/**
 * 行程地圖視圖（跨平台）
 *
 * @param markers 地圖標記列表
 * @param selectedMarker 當前選中的標記
 * @param onMarkerClick 標記點擊回調
 * @param modifier Modifier
 */
@Composable
expect fun ItineraryMapView(
    markers: List<MapMarker>,
    selectedMarker: MapMarker?,
    onMarkerClick: (MapMarker) -> Unit,
    modifier: Modifier = Modifier
)
