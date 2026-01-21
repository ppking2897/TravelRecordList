package com.example.myapplication.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.Hashtag

/**
 * 標籤列表元件
 */
@Composable
fun HashtagRow(
    hashtags: List<Hashtag>,
    onHashtagClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(hashtags) { hashtag ->
            HashtagChip(
                hashtag = hashtag,
                onClick = onHashtagClick
            )
        }
    }
}

/**
 * 單個標籤 Chip
 */
@Composable
fun HashtagChip(
    hashtag: Hashtag,
    onClick: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = { onClick?.invoke(hashtag.tag) },
        label = { Text("#${hashtag.tag}") },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}
