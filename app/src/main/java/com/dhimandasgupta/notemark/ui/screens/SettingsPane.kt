package com.dhimandasgupta.notemark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.presenter.SettingsUiModel
import com.dhimandasgupta.notemark.ui.PhoneLandscapePreview
import com.dhimandasgupta.notemark.ui.PhonePortraitPreview
import com.dhimandasgupta.notemark.ui.TabletExpandedLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletExpandedPortraitPreview
import com.dhimandasgupta.notemark.ui.TabletMediumLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletMediumPortraitPreview
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme

@Composable
fun SettingsPane(
    modifier: Modifier = Modifier,
    settingsUiModel: SettingsUiModel,
    onLogoutSuccessful: () -> Unit = {},
    onBackClicked: () -> Unit = {},
    onLogoutClicked: () -> Unit = {}
) {
    LaunchedEffect(key1 = settingsUiModel.logoutStatus) {
        if (settingsUiModel.logoutStatus == true) {
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
            ),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClicked
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                contentDescription = "Settings",
                tint = colorScheme.onSurface,
                modifier = Modifier.requiredSize(size = 32.dp)
            )
        }

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
    onLogoutClicked: () -> Unit = {}
) {
    Row(
        modifier = modifier
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
            .padding(
                vertical = 4.dp,
                horizontal = 16.dp
            )
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