package com.dhimandasgupta.notemark.ui.screens

import LoggedInUser
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.common.android.ConnectionState.Available
import com.dhimandasgupta.notemark.common.convertIsoOffsetToReadableFormat
import com.dhimandasgupta.notemark.common.extensions.formatUserName
import com.dhimandasgupta.notemark.database.NoteEntity
import com.dhimandasgupta.notemark.presenter.NoteListUiModel
import com.dhimandasgupta.notemark.statemachine.AppState
import com.dhimandasgupta.notemark.statemachine.LoggedInState
import com.dhimandasgupta.notemark.statemachine.NonLoggedInState
import com.dhimandasgupta.notemark.statemachine.NoteListAction
import com.dhimandasgupta.notemark.ui.PhoneLandscapePreview
import com.dhimandasgupta.notemark.ui.PhonePortraitPreview
import com.dhimandasgupta.notemark.ui.TabletExpandedLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletExpandedPortraitPreview
import com.dhimandasgupta.notemark.ui.TabletMediumLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletMediumPortraitPreview
import com.dhimandasgupta.notemark.ui.common.DeviceLayoutType
import com.dhimandasgupta.notemark.ui.common.getDeviceLayoutType
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkFAB
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkToolbarButton
import com.dhimandasgupta.notemark.ui.extendedTabletLandscape
import com.dhimandasgupta.notemark.ui.extendedTabletPortrait
import com.dhimandasgupta.notemark.ui.mediumTabletLandscape
import com.dhimandasgupta.notemark.ui.mediumTabletPortrait
import com.dhimandasgupta.notemark.ui.phoneLandscape
import com.dhimandasgupta.notemark.ui.phonePortrait
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlin.random.Random

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun NoteListPane(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    appState: AppState,
    noteListUiModel: NoteListUiModel,
    noteListAction: (NoteListAction) -> Unit = {},
    onFabClicked: () -> Unit = {},
    onLogoutClicked: () -> Unit = {},
) {
    LaunchedEffect(key1 = appState) {
        if (appState is NonLoggedInState) {
            onLogoutClicked()
            return@LaunchedEffect
        }
    }

    Box(
        modifier = modifier
            .background(color = colorResource(R.color.splash_blue_background))
            .fillMaxSize()
    ) {
        when (appState) {
            is NonLoggedInState -> throw IllegalStateException("This should not happen")
            is LoggedInState -> NoteListValidPane(
                modifier = Modifier,
                windowSizeClass = windowSizeClass,
                userName = appState.loggedInUser.userName.formatUserName(),
                noteListUiModel = noteListUiModel,
                noteListAction = noteListAction,
                onFabClicked = onFabClicked,
                onLogoutClicked = onLogoutClicked
            )
        }
    }
}

@Composable
private fun NoteListValidPane(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    userName: String,
    noteListUiModel: NoteListUiModel,
    noteListAction: (NoteListAction) -> Unit = {},
    onFabClicked: () -> Unit = {},
    onLogoutClicked: () -> Unit = {},
) {
    when (noteListUiModel.noteEntities.isEmpty()) {
        true -> NoteListWithEmptyNotes(
            modifier = modifier,
            userName = userName,
            onFabClicked = onFabClicked,
            onLogoutClicked = onLogoutClicked
        )
        else -> NoteListWithNotes(
            modifier = Modifier,
            windowSizeClass = windowSizeClass,
            userName = userName,
            noteListState = noteListUiModel,
            noteListAction = noteListAction,
            onFabClicked = onFabClicked,
            onLogoutClicked = onLogoutClicked
        )
    }
}

@Composable
private fun NoteListWithEmptyNotes(
    modifier: Modifier = Modifier,
    userName: String,
    onFabClicked: () -> Unit,
    onLogoutClicked: () -> Unit
) {
    NoNotes(
        modifier = modifier,
        userName = userName,
        onFabClicked = onFabClicked,
        onLogoutClicked = onLogoutClicked
    )
}

@Composable
fun NoteListWithNotes(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    userName: String,
    noteListState: NoteListUiModel,
    noteListAction: (NoteListAction) -> Unit = {},
    onFabClicked: () -> Unit = {},
    onLogoutClicked: () -> Unit = {},
) {
    val layoutType = getDeviceLayoutType(windowSizeClass)
    val columnCount = remember(layoutType) {
        when (layoutType) {
            DeviceLayoutType.PHONE_PORTRAIT -> 2
            else -> 3
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        NoteListPaneToolbar(
            modifier = Modifier,
            toolbarTitle = "NoteMark",
            userName = userName,
            onLogoutClicked = onLogoutClicked
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = WindowInsets.navigationBars.union(WindowInsets.displayCutout)
                        .asPaddingValues()
                        .calculateLeftPadding(LayoutDirection.Ltr),
                    end = WindowInsets.navigationBars.union(WindowInsets.displayCutout)
                        .asPaddingValues()
                        .calculateEndPadding(LayoutDirection.Ltr)
                )
        ) {
            NoteGrid(
                modifier = Modifier.fillMaxSize(),
                columnCount = columnCount,
                noteListUiModel = noteListState,
                noteListAction = noteListAction
            )
            NoteMarkFAB(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
                onClick = onFabClicked,
            )
        }
    }
}

@Composable
private fun NoteListPaneToolbar(
    modifier: Modifier = Modifier,
    toolbarTitle: String,
    userName: String,
    onLogoutClicked: () -> Unit,
) {
    Row(
        modifier = modifier
            .background(color = colorScheme.surfaceContainerLowest)
            .fillMaxWidth()
            .padding(
                top = WindowInsets.systemBars.union(WindowInsets.displayCutout)
                    .asPaddingValues()
                    .calculateTopPadding()
            )
            .padding(
                vertical = 4.dp,
                horizontal = 16.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = toolbarTitle,
            style = typography.titleMedium
        )

        NoteMarkToolbarButton(
            title = userName,
            onClick = onLogoutClicked
        )
    }
}

@Composable
private fun NoNotes(
    modifier: Modifier = Modifier,
    toolbarTitle: String = "NoteMark",
    userName: String = "DD",
    onLogoutClicked: () -> Unit,
    onFabClicked: () -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        NoteListPaneToolbar(
            modifier = Modifier,
            toolbarTitle = toolbarTitle,
            userName = userName,
            onLogoutClicked = onLogoutClicked
        )

        Text(
            text = "You’ve got an empty board, \n let’s place your first note on it!",
            style = typography.titleSmall,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    WindowInsets.displayCutout.union(WindowInsets.statusBars).union(
                        WindowInsets.navigationBars
                    ).asPaddingValues()
                )
                .padding(vertical = 96.dp, horizontal = 32.dp)
        )

        NoteMarkFAB(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            onClick = onFabClicked,
        )
    }
}

@Composable
private fun NoteGrid(
    modifier: Modifier = Modifier,
    columnCount: Int,
    noteListUiModel: NoteListUiModel,
    noteListAction: (NoteListAction) -> Unit = {},
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(count = columnCount),
        contentPadding = PaddingValues(16.dp),
        verticalItemSpacing = 16.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(
            items = noteListUiModel.noteEntities,
            key = { note -> note.id },
        ) { noteEntity ->
            NoteItem(
                modifier = Modifier,
                note = noteEntity
            )
        }

        item {
            Spacer(
                modifier = Modifier
                    .padding(
                        bottom = WindowInsets.navigationBars.union(WindowInsets.displayCutout)
                            .asPaddingValues()
                            .calculateBottomPadding()
                    )
            )
        }
    }
}

@Composable
fun NoteItem(
    modifier: Modifier = Modifier,
    note: NoteEntity
) {
    Column(
        modifier = modifier
            .clip(shape = shapes.medium)
            .background(color = colorScheme.surfaceContainerLowest)
            .padding(16.dp)
    ) {
        Text(
            text = convertIsoOffsetToReadableFormat(note.lastEditedAt),
            style = typography.bodyMedium,
            color = colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = note.title,
            style = typography.titleMedium,
            color = colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = note.content,
            style = typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Ellipsis,
            maxLines = Random.nextInt(3, 6)
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhonePortraitPreview
@Composable
private fun PhonePortraitPreview() {
    NoteMarkTheme {
        NoteListPane(
            modifier = Modifier,
            windowSizeClass = phonePortrait,
            appState = loggedInState,
            noteListUiModel = noteListUiModel
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhoneLandscapePreview
@Composable
private fun PhoneLandscapePreview() {
    NoteMarkTheme {
        NoteListPane(
            modifier = Modifier,
            windowSizeClass = phoneLandscape,
            appState = loggedInState,
            noteListUiModel = noteListUiModel
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumPortraitPreview
@Composable
private fun TabletMediumPortraitPreview() {
    NoteMarkTheme {
        NoteListPane(
            modifier = Modifier,
            windowSizeClass = mediumTabletPortrait,
            appState = loggedInState,
            noteListUiModel = noteListUiModel
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumLandscapePreview
@Composable
private fun TabletMediumLandscapePreview() {
    NoteMarkTheme {
        NoteListPane(
            modifier = Modifier,
            windowSizeClass = mediumTabletLandscape,
            appState = loggedInState,
            noteListUiModel = noteListUiModel
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedPortraitPreview
@Composable
private fun TabletExpandedPortraitPreview() {
    NoteMarkTheme {
        NoteListPane(
            modifier = Modifier,
            windowSizeClass = extendedTabletPortrait,
            appState = loggedInState,
            noteListUiModel = noteListUiModel
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedLandscapePreview
@Composable
private fun TabletExpandedLandscapePreview() {
    NoteMarkTheme {
        NoteListPane(
            modifier = Modifier,
            windowSizeClass = extendedTabletLandscape,
            appState = loggedInState,
            noteListUiModel = noteListUiModel
        )
    }
}

private val loggedInState = LoggedInState(
    loggedInUser = LoggedInUser(
        userName = "Dhiman",
        bearerTokens = BearerTokens("", "")
    ),
    connectionState = Available
)

private val noteListUiModel = NoteListUiModel(
    noteEntities = listOf(
        NoteEntity(
            id = 0,
            title = "This is a title for the Note",
            content = "This is content For the Note",
            createdAt = "19th Apr",
            lastEditedAt = "20th Apr",
            uuid = "e1ed931c-5cd1-4c87-8b13-83ab25f1307d"
        )
    )
)