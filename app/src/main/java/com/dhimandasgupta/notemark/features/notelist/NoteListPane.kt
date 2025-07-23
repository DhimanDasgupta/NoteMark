package com.dhimandasgupta.notemark.features.notelist

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.common.extensions.formatUserName
import com.dhimandasgupta.notemark.database.NoteEntity
import com.dhimandasgupta.notemark.ui.PhoneLandscapePreview
import com.dhimandasgupta.notemark.ui.PhonePortraitPreview
import com.dhimandasgupta.notemark.ui.TabletExpandedLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletExpandedPortraitPreview
import com.dhimandasgupta.notemark.ui.TabletMediumLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletMediumPortraitPreview
import com.dhimandasgupta.notemark.ui.common.DeviceLayoutType
import com.dhimandasgupta.notemark.ui.common.getDeviceLayoutType
import com.dhimandasgupta.notemark.ui.designsystem.LimitedText
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkFAB
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkToolbarButton
import com.dhimandasgupta.notemark.ui.designsystem.SafeIconButton
import com.dhimandasgupta.notemark.ui.designsystem.ThreeBouncingDots
import com.dhimandasgupta.notemark.ui.extendedTabletLandscape
import com.dhimandasgupta.notemark.ui.extendedTabletPortrait
import com.dhimandasgupta.notemark.ui.mediumTabletLandscape
import com.dhimandasgupta.notemark.ui.mediumTabletPortrait
import com.dhimandasgupta.notemark.ui.phoneLandscape
import com.dhimandasgupta.notemark.ui.phonePortrait
import kotlinx.collections.immutable.toPersistentList

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun NoteListPane(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    noteListUiModel: NoteListUiModel,
    noteListAction: (NoteListAction) -> Unit = {},
    onNoteClicked: (String) -> Unit = {},
    onFabClicked: () -> Unit = {},
    onSettingsClicked: () -> Unit = {},
    onProfileClicked: () -> Unit = {},
) {
    val updateNoteListUiModel by rememberUpdatedState(newValue = noteListUiModel)
    var noteDeleteId by remember { mutableStateOf<String?>(value = null) }

    Box(
        modifier = modifier
            .background(color = colorResource(id = R.color.splash_blue_background))
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        NoteListValidPane(
            modifier = Modifier,
            windowSizeClass = windowSizeClass,
            userName = noteListUiModel.userName?.formatUserName() ?: "",
            noteListUiModel = updateNoteListUiModel,
            onNoteClicked = onNoteClicked,
            onNoteLongClicked = { id ->
                noteDeleteId = id
            },
            onFabClicked = onFabClicked,
            onSettingsClicked = onSettingsClicked,
            onProfileClicked = onProfileClicked
        )

        // Dialog check
        if (!noteDeleteId.isNullOrEmpty()) {
            NoteDeleteDialog(
                noteId = updateNoteListUiModel.noteLongClickedUuid,
                onDelete = { _ ->
                    noteDeleteId?.let { id ->
                        noteListAction(NoteListAction.NoteDelete(uuid = id))
                    }
                    noteDeleteId = null
                },
                onDismiss = {
                    noteDeleteId = null
                }
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
    onNoteClicked: (String) -> Unit = {},
    onNoteLongClicked: (String) -> Unit = {},
    onFabClicked: () -> Unit = {},
    onSettingsClicked: () -> Unit = {},
    onProfileClicked: () -> Unit = {},
) {
    when (noteListUiModel.noteEntities.isEmpty()) {
        true -> NoteListWithEmptyNotes(
            modifier = modifier,
            userName = userName,
            onFabClicked = onFabClicked,
            showSyncProgress = noteListUiModel.showSyncProgress,
            onSettingsClicked = onSettingsClicked,
            onProfileClicked = onProfileClicked
        )

        else -> NoteListWithNotes(
            modifier = Modifier,
            windowSizeClass = windowSizeClass,
            userName = userName,
            noteListState = noteListUiModel,
            onNoteClicked = onNoteClicked,
            onNoteLongClicked = onNoteLongClicked,
            onFabClicked = onFabClicked,
            onSettingsClicked = onSettingsClicked,
            onProfileClicked = onProfileClicked
        )
    }
}

@Composable
private fun NoteListWithEmptyNotes(
    modifier: Modifier = Modifier,
    userName: String,
    showSyncProgress: Boolean,
    onFabClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onProfileClicked: () -> Unit
) {
    NoNotes(
        modifier = modifier,
        userName = userName,
        showSyncProgress = showSyncProgress,
        onFabClicked = onFabClicked,
        onSettingsClicked = onSettingsClicked,
        onProfileClicked = onProfileClicked
    )
}

@Composable
fun NoteListWithNotes(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    userName: String,
    noteListState: NoteListUiModel,
    onNoteClicked: (String) -> Unit = {},
    onNoteLongClicked: (String) -> Unit = {},
    onFabClicked: () -> Unit = {},
    onSettingsClicked: () -> Unit = {},
    onProfileClicked: () -> Unit = {},
) {
    val layoutType = getDeviceLayoutType(windowSizeClass)
    val columnCount = remember(key1 = layoutType) {
        when (layoutType) {
            DeviceLayoutType.PHONE_PORTRAIT -> 2
            else -> 3
        }
    }

    val maxLength = remember(key1 = layoutType) {
        when (layoutType) {
            DeviceLayoutType.TABLET_LAYOUT -> 250
            else -> 150
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        NoteListPaneToolbar(
            modifier = Modifier,
            toolbarTitle = "NoteMark",
            userName = userName,
            onSettingsClicked = onSettingsClicked,
            onProfileClicked = onProfileClicked
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = WindowInsets.navigationBars.union(insets = WindowInsets.displayCutout)
                        .asPaddingValues()
                        .calculateLeftPadding(LayoutDirection.Ltr),
                    end = WindowInsets.navigationBars.union(insets = WindowInsets.displayCutout)
                        .asPaddingValues()
                        .calculateEndPadding(LayoutDirection.Ltr)
                )
        ) {
            NoteGrid(
                modifier = Modifier.fillMaxSize(),
                columnCount = columnCount,
                maxLength = maxLength,
                noteListUiModel = noteListState,
                onNoteClicked = onNoteClicked,
                onNoteLongClicked = onNoteLongClicked
            )
            NoteMarkFAB(
                modifier = Modifier
                    .padding(all = 16.dp)
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
    onSettingsClicked: () -> Unit,
    onProfileClicked: () -> Unit,
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
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = toolbarTitle,
            style = typography.titleMedium,
            modifier = Modifier.weight(weight = 1f)
        )

        SafeIconButton(
            onClick = onSettingsClicked
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = "Settings",
                modifier = Modifier.requiredSize(size = 48.dp)
            )
        }

        Spacer(modifier = Modifier.width(width = 16.dp))

        NoteMarkToolbarButton(
            title = userName,
            onClick = onProfileClicked
        )
    }
}

@Composable
private fun NoNotes(
    modifier: Modifier = Modifier,
    toolbarTitle: String = "NoteMark",
    userName: String = "",
    showSyncProgress: Boolean,
    onSettingsClicked: () -> Unit,
    onProfileClicked: () -> Unit,
    onFabClicked: () -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        NoteListPaneToolbar(
            modifier = Modifier,
            toolbarTitle = toolbarTitle,
            userName = userName,
            onSettingsClicked = onSettingsClicked,
            onProfileClicked = onProfileClicked
        )

        when (showSyncProgress) {
            true -> Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .wrapContentSize()
                    .padding(all = 16.dp)
            ) {
                ThreeBouncingDots(
                    modifier = Modifier
                        .padding(all = 16.dp)
                        .wrapContentSize(),
                    dotColor1 = colorResource(id = R.color.splash_blue).copy(alpha = 0.5f),
                    dotColor2 = colorResource(id = R.color.splash_blue).copy(alpha = 0.75f),
                    dotColor3 = colorResource(id = R.color.splash_blue).copy(alpha = 1.0f)
                )
            }

            else -> Text(
                text = "You’ve got an empty board, \n let’s place your first note on it!",
                style = typography.titleSmall,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(
                        paddingValues = WindowInsets.displayCutout.union(insets = WindowInsets.statusBars)
                            .union(
                                insets = WindowInsets.navigationBars
                            ).asPaddingValues()
                    )
                    .padding(vertical = 96.dp, horizontal = 32.dp)
            )
        }

        NoteMarkFAB(
            modifier = Modifier
                .padding(all = 16.dp)
                .align(Alignment.BottomEnd),
            onClick = onFabClicked,
        )
    }
}

@Composable
private fun NoteGrid(
    modifier: Modifier = Modifier,
    columnCount: Int,
    maxLength: Int,
    noteListUiModel: NoteListUiModel,
    onNoteClicked: (String) -> Unit = {},
    onNoteLongClicked: (String) -> Unit = {}
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(count = columnCount),
        contentPadding = PaddingValues(all = 8.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        if (noteListUiModel.showSyncProgress) {
            item(
                span = StaggeredGridItemSpan.FullLine,
                key = "sync_progress",
                contentType = "sync_progress"
            ) {
                ThreeBouncingDots(
                    modifier = Modifier
                        .padding(all = 16.dp)
                        .wrapContentSize(),
                    dotColor1 = colorResource(id = R.color.splash_blue).copy(alpha = 0.5f),
                    dotColor2 = colorResource(id = R.color.splash_blue).copy(alpha = 0.75f),
                    dotColor3 = colorResource(id = R.color.splash_blue).copy(alpha = 1.0f)
                )
            }
        }

        items(
            items = noteListUiModel.noteEntities,
            key = { note -> note.id },
            contentType = { "notes" }
        ) { noteEntity ->
            NoteItem(
                modifier = Modifier,
                note = noteEntity,
                maxLength = maxLength,
                onNoteClicked = onNoteClicked,
                onNoteLongClicked = onNoteLongClicked
            )
        }

        item(
            span = StaggeredGridItemSpan.FullLine,
            key = "spacer",
            contentType = "spacer"
        ) {
            Spacer(
                modifier = Modifier
                    .height(
                        height = WindowInsets.navigationBars
                            .asPaddingValues()
                            .calculateBottomPadding()
                    )
            )
        }
    }
}

@Composable
private fun NoteItem(
    modifier: Modifier = Modifier,
    note: NoteEntity,
    maxLength: Int,
    onNoteClicked: (String) -> Unit = {},
    onNoteLongClicked: (String) -> Unit = {}
) {
    Column(
        modifier = modifier
            .clip(shape = shapes.medium)
            .background(color = colorScheme.surfaceContainerLowest)
            .combinedClickable(
                onClick = { onNoteClicked(note.uuid) },
                onLongClick = { onNoteLongClicked(note.uuid) }
            )
            .padding(all = 16.dp)
    ) {
        Text(
            text = note.lastEditedAt,
            style = typography.bodyMedium,
            color = colorScheme.primary
        )

        Spacer(modifier = Modifier.height(height = 8.dp))

        LimitedText(
            fullText = note.title,
            style = typography.titleMedium,
            color = colorScheme.onSurface,
            targetCharacterCount = maxLength
        )

        Spacer(modifier = Modifier.height(height = 4.dp))

        LimitedText(
            fullText = note.content,
            style = typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
            targetCharacterCount = maxLength
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
            noteListUiModel = noteListUiModel
        )
    }
}

private val noteListUiModel = NoteListUiModel(
    userName = "Dhiman",
    noteEntities = listOf(
        NoteEntity(
            id = 0,
            title = "This is a title for the Note",
            content = "This is content For the Note",
            createdAt = "19th Apr",
            lastEditedAt = "20th Apr",
            uuid = "e1ed931c-5cd1-4c87-8b13-83ab25f1307d",
            synced = true,
            markAsDeleted = false
        )
    ).toPersistentList(),
    noteLongClickedUuid = "e1ed931c-5cd1-4c87-8b13-83ab25f1307d",
    showSyncProgress = true
)
