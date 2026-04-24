package com.example.myapplication.feature.itinerary_list.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.presentation.components.SkeletonLine
import com.example.myapplication.core.presentation.components.shimmer
import com.example.myapplication.core.presentation.theme.CardStyle
import com.example.myapplication.core.presentation.theme.CornerRadius
import com.example.myapplication.core.presentation.theme.Spacing

/**
 * 行程列表載入中的骨架佔位，模擬 ItineraryDashboardCard 的佈局。
 */
@Composable
fun ItineraryListSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 3
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        repeat(itemCount) {
            ItineraryCardSkeleton()
        }
    }
}

@Composable
private fun ItineraryCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = CardStyle.listCardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(CornerRadius.sm))
                    .shimmer()
            )
            Column(
                modifier = Modifier.padding(vertical = Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                SkeletonLine(height = 18.dp, width = 200.dp)
                SkeletonLine(height = 14.dp, width = 140.dp)
                SkeletonLine(height = 12.dp, width = 180.dp)
            }
        }
    }
}
