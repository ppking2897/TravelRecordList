package com.example.myapplication.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onLocationSelected: (LocationSuggestion?) -> Unit,
    locationSearchService: LocationSearchService? = null,
    modifier: Modifier = Modifier,
    label: String = "地點名稱",
    placeholder: String = "輸入地點名稱搜尋...",
    debounceMs: Long = 800L,
) {
    var suggestions by remember { mutableStateOf<List<LocationSuggestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Debounced search
    LaunchedEffect(value) {
        // 空白或無服務時清空
        if (value.isBlank() || locationSearchService == null) {
            suggestions = emptyList()
            expanded = false
            isLoading = false
            return@LaunchedEffect
        }

        // 與上次搜尋相同時不重複搜尋
        if (value == searchQuery) return@LaunchedEffect

        // 先等待使用者停止輸入
        delay(debounceMs)

        // delay 後才顯示 loading，避免閃爍
        isLoading = true

        locationSearchService.searchByName(value)
            .onSuccess { results ->
                suggestions = results
                expanded = results.isNotEmpty()
                searchQuery = value
            }
            .onFailure {
                suggestions = emptyList()
                expanded = false
            }

        isLoading = false
    }

    ExposedDropdownMenuBox(
        expanded = expanded && suggestions.isNotEmpty(),
        onExpandedChange = { /* 由搜尋結果控制展開 */ },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                // 清除已選擇的地點
                if (newValue != value) {
                    onLocationSelected(null)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable),
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
                            expanded = false
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

        ExposedDropdownMenu(
            expanded = expanded && suggestions.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 300.dp),
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = suggestion.name,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = suggestion.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    onClick = {
                        onValueChange(suggestion.name)
                        onLocationSelected(suggestion)
                        expanded = false
                        searchQuery = suggestion.name
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                )
            }
        }
    }
}
