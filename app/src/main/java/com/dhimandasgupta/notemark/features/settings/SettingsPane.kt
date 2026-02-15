package com.dhimandasgupta.notemark.features.settings

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.common.convertNoteTimestampToReadableFormat
import com.dhimandasgupta.notemark.common.extensions.setDarkStatusBarIcons
import com.dhimandasgupta.notemark.features.launcher.AppAction
import com.dhimandasgupta.notemark.proto.Sync
import com.dhimandasgupta.notemark.ui.WindowSizePreviews
import com.dhimandasgupta.notemark.ui.common.lifecycleAwareDebouncedClickable
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter

@OptIn(FlowPreview::class)
@Composable
fun SettingsPane(
    modifier: Modifier = Modifier,
    settingsUiModel: () -> SettingsUiModel,
    settingsAction: (AppAction) -> Unit = {},
    onDeleteNoteCheckChanged: () -> Unit = {},
    onLogoutSuccessful: () -> Unit = {},
    onBackClicked: () -> Unit = {},
    onLogoutClicked: () -> Unit = {}
) {
    val context = LocalActivity.current
    SideEffect { context?.setDarkStatusBarIcons(true) }

    val updatedSettingsUiModel by rememberUpdatedState(newValue = settingsUiModel)
    val updatedOnLogoutSuccessful by rememberUpdatedState(newValue = onLogoutSuccessful)

    var showSyncInterval by remember { mutableStateOf(value = false) }

    LaunchedEffect(key1 = Unit) {
        snapshotFlow { updatedSettingsUiModel().logoutStatus }
            .debounce(timeoutMillis = 100)
            .filter { status -> status == true }
            .collect { _ ->
                updatedOnLogoutSuccessful()
            }
    }

    Column(
        modifier = modifier
            .background(color = colorScheme.surfaceContainerLowest)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
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
            onDeleteNoteCheckChanged = onDeleteNoteCheckChanged,
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
                start = WindowInsets.systemBars.union(insets = WindowInsets.displayCutout)
                    .asPaddingValues()
                    .calculateLeftPadding(LayoutDirection.Ltr),
                top = WindowInsets.systemBars.union(insets = WindowInsets.displayCutout)
                    .asPaddingValues()
                    .calculateTopPadding(),
                end = WindowInsets.systemBars.union(insets = WindowInsets.displayCutout)
                    .asPaddingValues()
                    .calculateEndPadding(LayoutDirection.Ltr)
            )
            .padding(
                vertical = 4.dp,
                horizontal = 16.dp
            )
            .lifecycleAwareDebouncedClickable(onClick = onBackClicked),
        horizontalArrangement = Arrangement.spacedBy(space = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_back_arrow),
            contentDescription = "Settings",
            tint = colorScheme.onSurface,
            modifier = Modifier.requiredSize(size = 32.dp)
        )

        Text(
            text = "Settings",
            style = typography.titleMedium,
            color = colorScheme.onSurface,
            modifier = Modifier.weight(weight = 1f)
        )
    }
}

@Composable
private fun SettingsBody(
    modifier: Modifier = Modifier,
    settingsUiModel: () -> SettingsUiModel,
    showSyncInterval: Boolean = false,
    toggleSyncIntervalVisibility: () -> Unit = {},
    onSyncIntervalSelected: (String) -> Unit = {},
    onDeleteNoteCheckChanged: () -> Unit = {},
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
                    start = WindowInsets.systemBars.union(insets = WindowInsets.displayCutout)
                        .asPaddingValues()
                        .calculateLeftPadding(LayoutDirection.Ltr),
                    end = WindowInsets.systemBars.union(insets = WindowInsets.displayCutout)
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
            SyncIntervalRow(
                modifier = Modifier,
                settingsUiModel = settingsUiModel,
                toggleSyncIntervalVisibility = toggleSyncIntervalVisibility
            )

            Divider()

            // Sync Data
            SyncDataRow(
                modifier = Modifier,
                settingsUiModel = settingsUiModel
            )

            Divider()

            // Delete Local Data
            DeleteLocalDataRow(
                modifier = Modifier,
                settingsUiModel = settingsUiModel,
                onCheckChange = onDeleteNoteCheckChanged
            )

            Divider()

            // Logout
            LogoutRow(
                modifier = Modifier,
                isConnected = settingsUiModel().isConnected,
                onLogoutClicked = onLogoutClicked
            )

            // AppVersion
            settingsUiModel().appVersionName?.let { appVersionName ->
                AppVersion(
                    modifier = Modifier,
                    appVersionName = appVersionName
                )
            }
        }

        if (showSyncInterval) {
            SyncDropDown(
                modifier = Modifier,
                selectedSyncInterval = settingsUiModel().selectedSyncInterval,
                syncIntervals = settingsUiModel().syncIntervals,
                toggleDropDownVisibility = toggleSyncIntervalVisibility,
                onDropDownItemSelected = onSyncIntervalSelected
            )
        }
    }
}

@Composable
private fun SyncIntervalRow(
    modifier: Modifier = Modifier,
    settingsUiModel: () -> SettingsUiModel,
    toggleSyncIntervalVisibility: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 8.dp))
            .combinedClickable(
                onClick = toggleSyncIntervalVisibility
            )
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_clock),
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
                .weight(weight = 1f)
        )

        Text(
            text = settingsUiModel().selectedSyncInterval,
            style = typography.bodyLarge,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.wrapContentSize()
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_right),
            contentDescription = "Sync Interval",
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(start = 8.dp)
                .requiredSize(size = 32.dp)
        )
    }
}

@Composable
private fun SyncDataRow(
    modifier: Modifier = Modifier,
    settingsUiModel: () -> SettingsUiModel,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 8.dp))
            .combinedClickable(
                onClick = {}
            )
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "InfiniteTransition")
        val rotationAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "RotationAnimation"
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_sync),
            contentDescription = "Settings",
            tint = colorScheme.onSurface,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .requiredSize(size = 24.dp)
                .then(
                    other = Modifier.graphicsLayer {
                        rotationZ = if (settingsUiModel().isSyncing) rotationAngle else 0f
                    }
                )
        )

        Column {
            Text(
                text = "Sync Data",
                style = typography.titleSmall,
                color = colorScheme.onSurface,
                modifier = Modifier.wrapContentSize()
            )

            val configuration = LocalConfiguration.current
            val locale by remember(key1 = configuration) {
                mutableStateOf(
                    configuration.locales.getFirstMatch(arrayOf("en"))
                        ?: configuration.locales.get(0)
                )
            }

            Text(
                text = "Last synced: ${
                    convertNoteTimestampToReadableFormat(
                        locale = locale,
                        isoOffsetDateTimeString = settingsUiModel().lastSynced
                    )
                }",
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.wrapContentSize()
            )
        }
    }
}

@Composable
private fun DeleteLocalDataRow(
    modifier: Modifier = Modifier,
    settingsUiModel: () -> SettingsUiModel,
    onCheckChange: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 8.dp))
            .combinedClickable(
                onClick = onCheckChange
            )
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Delete local notes when logging out?",
            style = typography.titleSmall,
            color = colorScheme.error,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .weight(weight = 1f)
        )

        Icon(
            imageVector = if (settingsUiModel().deleteLocalNotesOnLogout) Icons.Default.Check else Icons.Filled.Close,
            contentDescription = if (settingsUiModel().deleteLocalNotesOnLogout) "Checked" else "Unchecked",
            tint = colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun LogoutRow(
    modifier: Modifier = Modifier,
    isConnected: Boolean,
    onLogoutClicked: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 8.dp))
            .combinedClickable(
                onClick = {
                    if (isConnected) onLogoutClicked() else Unit
                }
            )
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_log_out),
            contentDescription = "Settings",
            tint = if (isConnected) colorScheme.error else colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .requiredSize(size = 32.dp)
        )

        Text(
            text = "Log out",
            style = typography.titleMedium,
            color = if (isConnected) colorScheme.error else colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(weight = 1f)
        )
    }
}

@Composable
private fun AppVersion(
    modifier: Modifier = Modifier,
    appVersionName: String
) {
    Text(
        text = "App Version - $appVersionName",
        style = typography.titleSmall,
        color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        textAlign = TextAlign.Right,
        modifier = modifier
            .padding(all = 16.dp)
            .fillMaxWidth()
    )
}

@Composable
private fun Divider(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(height = 1.dp)
            .background(color = colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
    )
}

@WindowSizePreviews
@Composable
private fun SettingsPanePreview() {
    NoteMarkTheme {
        SettingsPane(
            modifier = Modifier,
            settingsUiModel = defaultSettingsUiModel
        )
    }
}

private val defaultSettingsUiModel = { SettingsUiModel.Empty.copy(appVersionName = "Some Version") }