package com.example.myapplication.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    initialTime: LocalTime? = null
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime?.hour ?: 12,
        initialMinute = initialTime?.minute ?: 0,
        is24Hour = true
    )
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("選擇時間") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.padding(16.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val time = LocalTime(
                    hour = timePickerState.hour,
                    minute = timePickerState.minute
                )
                onConfirm(time)
            }) {
                Text("確定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("取消")
            }
        }
    )
}
