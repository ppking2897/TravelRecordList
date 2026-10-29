package io.github.ppking2897.travelrecord.core.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.ppking2897.travelrecord.core.presentation.theme.Spacing
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Hashtag

/**
 * 標籤列表元件
 *
 * 給了 [onTagClick] 才能點（行程詳情用來依標籤篩選）；沒給時只顯示，點到等於點外面的卡片。
 *
 * @param selectedTag 正在篩的標籤（不含 #），顯示成選中的樣子
 */
@Composable
fun HashtagRow(
    hashtags: List<Hashtag>,
    selectedTag: String?,
    onTagClick: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(hashtags) { hashtag ->
            HashtagChip(
                hashtag = hashtag,
                selected = hashtag.tag == selectedTag,
                onClick = onTagClick?.let { { it(hashtag.tag) } }
            )
        }
    }
}

/**
 * 單個標籤
 */
@Composable
fun HashtagChip(
    hashtag: Hashtag,
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
        shape = CircleShape
    ) {
        Text(
            text = "#${hashtag.tag}",
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
