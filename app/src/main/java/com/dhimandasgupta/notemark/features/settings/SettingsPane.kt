package com.dhimandasgupta.notemark.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.features.launcher.AppAction
import com.dhimandasgupta.notemark.proto.Sync
import com.dhimandasgupta.notemark.ui.PhoneLandscapePreview
import com.dhimandasgupta.notemark.ui.PhonePortraitPreview
import com.dhimandasgupta.notemark.ui.TabletExpandedLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletExpandedPortraitPreview
import com.dhimandasgupta.notemark.ui.TabletMediumLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletMediumPortraitPreview
import com.dhimandasgupta.notemark.ui.common.lifecycleAwareDebouncedClickable
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme

@Composable
fun SettingsPane(
    modifier: Modifier = Modifier,
    settingsUiModel: SettingsUiModel,
    settingsAction: (AppAction) -> Unit = {},
    onLogoutSuccessful: () -> Unit = {},
    onBackClicked: () -> Unit = {},
    onLogoutClicked: () -> Unit = {}
) {
    val updatedSettingsUiModel by rememberUpdatedState(settingsUiModel)

    var showSyncInterval by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = updatedSettingsUiModel.logoutStatus) {
        if (updatedSettingsUiModel.logoutStatus == true) {
            onLogoutSuccessful()
            return@LaunchedEffect
        }
    }

    Column(
        modifier = modifier
            .background(color = colorScheme.surfaceContainerLowest)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SettingsToolbar(
            onBackClicked = onBackClicked
        )
        SettingsBody(
            settingsUiModel = settingsUiModel,
            showSyncInterval = showSyncInterval,
            toggleSyncIntervalVisibility = { showSyncInterval = !showSyncInterval },
            onSyncIntervalSelected = { label ->
                settingsAction(
                    AppAction.UpdateSync(
                        syncDuration = when (label) {
                            "15 Minutes" -> Sync.SyncDuration.SYNC_DURATION_FIFTEEN_MINUTES
                            "30 Minutes" -> Sync.SyncDuration.SYNC_DURATION_THIRTY_MINUTES
                            "1 Hour" -> Sync.SyncDuration.SYNC_DURATION_ONE_HOUR
                            else -> Sync.SyncDuration.SYNC_DURATION_MANUAL
                        }
                    )
                )
            },
            onLogoutClicked = onLogoutClicked
        )
    }
}

@Composable
private fun SettingsToolbar(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .background(color = colorScheme.surfaceContainerLowest)
            .fillMaxWidth()
            .padding(
                start = WindowInsets.systemBars.union(WindowInsets.displayCutout)
                    .asPaddingValues()
                    .calculateLeftPadding(LayoutDirection.Ltr),
                top = WindowInsets.systemBars.union(WindowInsets.displayCutout)
                    .asPaddingValues()
                    .calculateTopPadding(),
                end = WindowInsets.systemBars.union(WindowInsets.displayCutout)
                    .asPaddingValues()
                    .calculateEndPadding(LayoutDirection.Ltr)
            )
            .padding(
                vertical = 4.dp,
                horizontal = 8.dp
            )
            .lifecycleAwareDebouncedClickable(onClick = onBackClicked),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            contentDescription = "Settings",
            tint = colorScheme.onSurface,
            modifier = Modifier.requiredSize(size = 32.dp)
        )

        Text(
            text = "Settings",
            style = typography.titleMedium,
            color = colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SettingsBody(
    modifier: Modifier = Modifier,
    settingsUiModel: SettingsUiModel,
    showSyncInterval: Boolean = false,
    toggleSyncIntervalVisibility: () -> Unit = {},
    onSyncIntervalSelected: (String) -> Unit = {},
    onLogoutClicked: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .background(color = colorScheme.surfaceContainerLowest)
                .fillMaxWidth()
                .padding(
                    start = WindowInsets.systemBars.union(WindowInsets.displayCutout)
                        .asPaddingValues()
                        .calculateLeftPadding(LayoutDirection.Ltr),
                    end = WindowInsets.systemBars.union(WindowInsets.displayCutout)
                        .asPaddingValues()
                        .calculateEndPadding(LayoutDirection.Ltr)
                )
                .scrollable(
                    state = rememberScrollState(),
                    orientation = Orientation.Vertical
                )
                .padding(horizontal = 16.dp)
        ) {
            // Sync Interval
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .combinedClickable(
                        onClick = toggleSyncIntervalVisibility
                    ),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_clock),
                    contentDescription = "Sync Interval",
                    tint = colorScheme.onSurface,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .requiredSize(size = 24.dp)
                )

                Text(
                    text = "Sync Interval",
                    style = typography.titleSmall,
                    color = colorScheme.onSurface,
                    modifier = Modifier.wrapContentSize()
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Text(
                    text = settingsUiModel.selectedSyncInterval,
                    style = typography.bodyLarge,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.wrapContentSize()
                )

                Icon(
                    painter = painterResource(R.drawable.ic_right),
                    contentDescription = "Sync Interval",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .requiredSize(size = 32.dp)
                )
            }

            Spacer(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color = colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    .padding(bottom = 8.dp)
            )

            // Sync Data
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .combinedClickable(
                        onClick = {}
                    ),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_sync),
                    contentDescription = "Settings",
                    tint = colorScheme.onSurface,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .requiredSize(size = 24.dp)
                )

                Column {
                    Text(
                        text = "Sync Data",
                        style = typography.titleSmall,
                        color = colorScheme.onSurface,
                        modifier = Modifier.wrapContentSize()
                    )

                    Text(
                        text = "Last synced: ${settingsUiModel.lastSynced}",
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.wrapContentSize()
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color = colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    .padding(bottom = 8.dp)
            )

            // Logout
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .combinedClickable(
                        onClick = onLogoutClicked
                    ),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_log_out),
                    contentDescription = "Settings",
                    tint = colorScheme.error,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .requiredSize(size = 32.dp)
                )

                Text(
                    text = "Log out",
                    style = typography.titleMedium,
                    color = colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (showSyncInterval) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopEnd) // Aligns the IconButton and thus the menu
                    .padding(16.dp)
            ) {
                DropdownMenu(
                    modifier = Modifier.background(color = colorScheme.surfaceContainerLowest),
                    expanded = true,
                    offset = DpOffset(x = 0.dp, y = 0.dp),
                    onDismissRequest = toggleSyncIntervalVisibility,
                ) {
                    settingsUiModel.syncIntervals.forEach { label ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = label,
                                    style = typography.bodyLarge
                                )
                            },
                            onClick = { onSyncIntervalSelected(label) },
                            trailingIcon = {
                                if (label == settingsUiModel.selectedSyncInterval) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = colorScheme.primary
                                    )
                                } else null
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhonePortraitPreview
@Composable
private fun PhonePortraitPreview() {
    NoteMarkTheme {
        SettingsPane(
            modifier = Modifier,
            settingsUiModel = defaultSettingsUiModel
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhoneLandscapePreview
@Composable
private fun PhoneLandscapePreview() {
    NoteMarkTheme {
        SettingsPane(
            modifier = Modifier,
            settingsUiModel = defaultSettingsUiModel
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumPortraitPreview
@Composable
private fun TabletMediumPortraitPreview() {
    NoteMarkTheme {
        SettingsPane(
            modifier = Modifier,
            settingsUiModel = defaultSettingsUiModel
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumLandscapePreview
@Composable
private fun TabletMediumLandscapePreview() {
    NoteMarkTheme {
        SettingsPane(
            modifier = Modifier,
            settingsUiModel = defaultSettingsUiModel
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedPortraitPreview
@Composable
private fun TabletExpandedPortraitPreview() {
    NoteMarkTheme {
        SettingsPane(
            modifier = Modifier,
            settingsUiModel = defaultSettingsUiModel
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedLandscapePreview
@Composable
private fun TabletExpandedLandscapePreview() {
    NoteMarkTheme {
        SettingsPane(
            modifier = Modifier,
            settingsUiModel = defaultSettingsUiModel
        )
    }
}

private val defaultSettingsUiModel = SettingsUiModel.Empty