package io.github.ppking2897.travelrecord.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.ppking2897.travelrecord.core.presentation.theme.CardStyle
import io.github.ppking2897.travelrecord.core.presentation.theme.IconSize
import io.github.ppking2897.travelrecord.core.presentation.theme.Spacing
import kotlinx.coroutines.launch

/**
 * 滑動手勢的總開關，行程卡片與項目卡片共用
 *
 * 2026-11-04 先關閉：滑動太容易誤觸。刪除、編輯仍可用卡片上的選單操作。
 * 重新開放前要先決定怎麼降低誤觸（例如提高觸發距離）。
 */
private const val SWIPE_ACTIONS_ENABLED = false

/**
 * 卡片左滑刪除、右滑編輯
 *
 * 放手後只呼叫 callback 並讓卡片回到原位，不把卡片移走：刪除要先確認，
 * 卡片消失是確認後列表重新讀取的結果，按取消時卡片本來就在原位。
 * 卡片上原本的選單照樣保留，不會滑的人也能操作。
 */
@Composable
fun SwipeActionsBox(
    onSwipeToDelete: () -> Unit,
    onSwipeToEdit: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // 用 remember 不跨轉螢幕保存：存的是「已滑出」的話，畫面回來會再觸發一次刪除或編輯
    val positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    val state = remember { SwipeToDismissBoxState(SwipeToDismissBoxValue.Settled, positionalThreshold) }
    val scope = rememberCoroutineScope()

    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        gesturesEnabled = SWIPE_ACTIONS_ENABLED && enabled,
        onDismiss = { direction ->
            when (direction) {
                SwipeToDismissBoxValue.EndToStart -> onSwipeToDelete()
                SwipeToDismissBoxValue.StartToEnd -> onSwipeToEdit()
                SwipeToDismissBoxValue.Settled -> Unit
            }
            scope.launch { state.reset() }
        },
        backgroundContent = {
            when (state.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> SwipeActionBackground(
                    icon = Icons.Default.Delete,
                    label = "刪除",
                    alignEnd = true,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
                SwipeToDismissBoxValue.StartToEnd -> SwipeActionBackground(
                    icon = Icons.Default.Edit,
                    label = "編輯",
                    alignEnd = false,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                SwipeToDismissBoxValue.Settled -> Unit
            }
        }
    ) {
        content()
    }
}

/** 滑動時卡片後面露出的底：圖示加文字，靠滑開的那一側 */
@Composable
private fun SwipeActionBackground(
    icon: ImageVector,
    label: String,
    alignEnd: Boolean,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(CardStyle.cornerRadius)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            horizontalArrangement = if (alignEnd) {
                Arrangement.spacedBy(Spacing.sm, Alignment.End)
            } else {
                Arrangement.spacedBy(Spacing.sm, Alignment.Start)
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(IconSize.sm))
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
