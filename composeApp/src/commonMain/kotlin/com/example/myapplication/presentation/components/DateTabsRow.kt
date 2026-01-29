package com.example.myapplication.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.util.toDateList
import com.example.myapplication.util.toShortString
import kotlinx.datetime.LocalDate

@Composable
fun DateTabsRow(
    dateRange: ClosedRange<LocalDate>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    val dates = dateRange.toDateList()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "全部" tab
        FilterChip(
            selected = selectedDate == null,
            onClick = { onDateSelected(null) },
            label = { Text("全部") }
        )
        
        // 日期 tabs
        dates.forEach { date ->
            FilterChip(
                selected = selectedDate == date,
                onClick = { onDateSelected(date) },
                label = { Text(date.toShortString()) }
            )
        }
    }
}
