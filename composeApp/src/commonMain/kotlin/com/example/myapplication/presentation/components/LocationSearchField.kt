package com.example.myapplication.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.service.LocationSearchService
import com.example.myapplication.domain.service.LocationSuggestion
import kotlinx.coroutines.delay

/**
 * 智慧地址輸入元件
 *
 * 提供地點搜尋功能，輸入時即時顯示建議，選擇後自動填入地點資訊
 *
 * @param value 當前輸入值
 * @param onValueChange 輸入值變更回調
 * @param onLocationSelected 選擇地點後的回調，傳入選中的地點建議
 * @param locationSearchService 地點搜尋服務（可選，如果未提供則只能手動輸入）
 * @param modifier Modifier
 * @param label 輸入框標籤
 * @param placeholder 輸入框提示文字
 * @param debounceMs 輸入防抖延遲（毫秒）
 */
@Composable
fun LocationSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onLocationSelected: (LocationSuggestion?) -> Unit,
    locationSearchService: LocationSearchService? = null,
    modifier: Modifier = Modifier,
    label: String = "地點名稱",
    placeholder: String = "輸入地點名稱搜尋...",
    debounceMs: Long = 500L,
) {
    var suggestions by remember { mutableStateOf<List<LocationSuggestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showDropdown by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Debounced search
    LaunchedEffect(value) {
        if (value.isBlank() || locationSearchService == null) {
            suggestions = emptyList()
            showDropdown = false
            return@LaunchedEffect
        }

        // 只有當輸入與上次搜尋不同時才搜尋
        if (value == searchQuery) return@LaunchedEffect

        isLoading = true
        delay(debounceMs)

        locationSearchService.searchByName(value)
            .onSuccess { results ->
                suggestions = results
                showDropdown = results.isNotEmpty()
                searchQuery = value
            }
            .onFailure {
                suggestions = emptyList()
                showDropdown = false
            }

        isLoading = false
    }

    Box(modifier = modifier) {
        Column {
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    onValueChange(newValue)
                    // 清除已選擇的地點
                    if (newValue != value) {
                        onLocationSelected(null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(label) },
                placeholder = { Text(placeholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    when {
                        isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(8.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                        value.isNotEmpty() -> {
                            IconButton(onClick = {
                                onValueChange("")
                                onLocationSelected(null)
                                suggestions = emptyList()
                                showDropdown = false
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "清除",
                                )
                            }
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                            )
                        }
                    }
                },
                singleLine = true,
            )

            // 搜尋建議下拉選單
            DropdownMenu(
                expanded = showDropdown && suggestions.isNotEmpty(),
                onDismissRequest = { showDropdown = false },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    suggestions.forEachIndexed { index, suggestion ->
                        LocationSuggestionItem(
                            suggestion = suggestion,
                            onClick = {
                                onValueChange(suggestion.name)
                                onLocationSelected(suggestion)
                                showDropdown = false
                                searchQuery = suggestion.name
                            },
                        )
                        if (index < suggestions.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

/**
 * 地點建議項目
 */
@Composable
private fun LocationSuggestionItem(
    suggestion: LocationSuggestion,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = suggestion.name,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        supportingContent = {
            Text(
                text = suggestion.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
    )
}
