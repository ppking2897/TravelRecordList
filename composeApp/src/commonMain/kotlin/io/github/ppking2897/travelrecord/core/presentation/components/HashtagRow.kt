package io.github.ppking2897.travelrecord.core.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ppking2897.travelrecord.core.presentation.theme.Spacing
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Hashtag

/**
 * 標籤列表元件
 *
 * 只顯示、不能點：依標籤篩選的功能拿掉了，做成按鈕的樣子會讓人以為點了有反應。
 */
@Composable
fun HashtagRow(
    hashtags: List<Hashtag>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(hashtags) { hashtag ->
            HashtagChip(hashtag = hashtag)
        }
    }
}

/**
 * 單個標籤
 */
@Composable
fun HashtagChip(
    hashtag: Hashtag,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = CircleShape
    ) {
        Text(
            text = "#${hashtag.tag}",
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
