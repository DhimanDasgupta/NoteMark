package com.dhimandasgupta.notemark.features.notelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dhimandasgupta.notemark.ui.PhonePortraitPreview
import com.dhimandasgupta.notemark.ui.common.lifecycleAwareDebouncedClickable
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme

@Composable
fun NoteDeleteDialog(
    modifier: Modifier = Modifier,
    noteId: String,
    onDelete: (String) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Column(
            modifier = modifier
                .clip(shape = shapes.medium)
                .wrapContentSize()
                .background(color = colorScheme.surfaceContainerLowest)
                .padding(all = 32.dp)
        ) {
            Text(
                text = "Are you sure you want to delete this note?",
                style = typography.titleMedium
            )

            Spacer(modifier = Modifier.height(height = 16.dp))

            Text(
                text = "Click Discard to delete your note, else click Cancel to keep editing.",
                style = typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(height = 32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                Text(
                    text = "Keep Editing",
                    style = typography.bodyMedium,
                    color = colorScheme.primary,
                    modifier = Modifier.lifecycleAwareDebouncedClickable { onDismiss() }
                )

                Spacer(modifier = Modifier.width(width = 32.dp))

                Text(
                    text = "Discard",
                    style = typography.bodyMedium,
                    color = colorScheme.error,
                    modifier = Modifier.lifecycleAwareDebouncedClickable {
                        onDelete(noteId)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhonePortraitPreview
@Composable
private fun DeleteDialogPreview() {
    NoteMarkTheme {
        NoteDeleteDialog(
            modifier = Modifier,
            noteId = "e1ed931c-5cd1-4c87-8b13-83ab25f1307d"
        )
    }
}