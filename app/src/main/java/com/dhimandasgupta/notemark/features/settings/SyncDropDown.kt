package com.dhimandasgupta.notemark.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SyncDropDown(
    modifier: Modifier = Modifier,
    selectedSyncInterval: String,
    syncIntervals: ImmutableList<String>,
    toggleDropDownVisibility: () -> Unit,
    onDropDownItemSelected: (String) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd) // Aligns the IconButton and thus the menu
            .padding(all = 16.dp)
            .semantics(true) {
                testTagsAsResourceId = true
                testTag = "Duration drop down"
            }
    ) {
        DropdownMenu(
            modifier = Modifier.background(color = colorScheme.surfaceContainerLowest),
            expanded = true,
            offset = DpOffset(x = 0.dp, y = 0.dp),
            onDismissRequest = toggleDropDownVisibility,
        ) {
            syncIntervals.forEach { label ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            style = typography.bodyLarge
                        )
                    },
                    onClick = {
                        onDropDownItemSelected(label)
                        toggleDropDownVisibility()
                    },
                    trailingIcon = {
                        if (label == selectedSyncInterval) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier.semantics {
                        testTagsAsResourceId = true
                        testTag = "DropDown_$label"
                    }
                )
            }
        }
    }
}