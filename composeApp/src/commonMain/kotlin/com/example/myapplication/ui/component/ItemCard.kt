package com.example.myapplication.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.ItineraryItem

@Composable
fun ItemCard(
    item: ItineraryItem,
    onToggleComplete: (String) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isCompleted,
                        onCheckedChange = { onToggleComplete(item.id) }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = item.activity,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = item.location.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // 顯示時間資訊
                        val timeText = when {
                            item.arrivalTime != null && item.departureTime != null -> 
                                "${item.arrivalTime} - ${item.departureTime}"
                            item.arrivalTime != null -> 
                                "到達 ${item.arrivalTime}"
                            item.departureTime != null -> 
                                "離開 ${item.departureTime}"
                            else -> null
                        }
                        timeText?.let { text ->
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Row {
                    IconButton(onClick = { onEdit(item.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "編輯")
                    }
                    IconButton(onClick = { onDelete(item.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "刪除")
                    }
                }
            }
            
            // Google Maps 縮圖
            item.location.address?.let { address ->
                GoogleMapThumbnail(
                    address = address,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
            
            // 備註
            if (item.notes.isNotBlank()) {
                Text(
                    text = item.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun GoogleMapThumbnail(
    address: String,
    modifier: Modifier = Modifier
) {
    // Google Maps Static API URL
    val mapUrl = "https://maps.googleapis.com/maps/api/staticmap?" +
            "center=${address.replace(" ", "+")}" +
            "&zoom=15" +
            "&size=600x300" +
            "&markers=color:red%7C${address.replace(" ", "+")}" +
            "&key=YOUR_GOOGLE_MAPS_API_KEY"
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 暫時顯示地址文字，實際使用時需要載入圖片
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "點擊查看地圖",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
