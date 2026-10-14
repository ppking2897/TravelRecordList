package com.example.myapplication.feature.itinerary_add_edit.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.presentation.components.CoverPhotoSection
import kotlinx.datetime.LocalDate

/**
 * 行程新增/編輯表單共用內容
 *
 * 由 `ItineraryAddEditScreen` 使用，Preview 也直接使用本 composable 以確保
 * 預覽與實際畫面結構一致（包含封面照片區塊）。
 */
@Composable
fun ItineraryFormContent(
    coverPhotoPath: String?,
    title: String,
    description: String,
    startDate: LocalDate?,
    endDate: LocalDate?,
    titleError: String?,
    dateError: String?,
    error: String?,
    isLoading: Boolean,
    isEditMode: Boolean,
    showDraftSaved: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onSelectCoverPhoto: () -> Unit,
    onRemoveCoverPhoto: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CoverPhotoSection(
            coverPhotoPath = coverPhotoPath,
            onSelectPhoto = onSelectCoverPhoto,
            onRemovePhoto = onRemoveCoverPhoto
        )

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("行程標題 *") },
            modifier = Modifier.fillMaxWidth(),
            isError = titleError != null,
            supportingText = titleError?.let { { Text(it) } }
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("描述") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            maxLines = 5
        )

        OutlinedTextField(
            value = startDate?.toString() ?: "",
            onValueChange = {},
            label = { Text("開始日期") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                TextButton(onClick = onStartDateClick) {
                    Text("選擇")
                }
            }
        )

        OutlinedTextField(
            value = endDate?.toString() ?: "",
            onValueChange = {},
            label = { Text("結束日期") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            isError = dateError != null,
            supportingText = dateError?.let { { Text(it) } },
            trailingIcon = {
                TextButton(onClick = onEndDateClick) {
                    Text("選擇")
                }
            }
        )

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        AnimatedVisibility(
            visible = showDraftSaved && !isEditMode,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = "✓ 已自動儲存草稿",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("儲存")
            }
        }
    }
}
