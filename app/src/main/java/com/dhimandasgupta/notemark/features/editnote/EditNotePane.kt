package com.dhimandasgupta.notemark.features.editnote

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.common.extensions.lockToLandscape
import com.dhimandasgupta.notemark.common.extensions.turnOffImmersiveMode
import com.dhimandasgupta.notemark.common.extensions.turnOnImmersiveMode
import com.dhimandasgupta.notemark.common.extensions.unlockOrientation
import com.dhimandasgupta.notemark.database.NoteEntity
import com.dhimandasgupta.notemark.ui.PhoneLandscapePreview
import com.dhimandasgupta.notemark.ui.PhonePortraitPreview
import com.dhimandasgupta.notemark.ui.TabletExpandedLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletExpandedPortraitPreview
import com.dhimandasgupta.notemark.ui.TabletMediumLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletMediumPortraitPreview
import com.dhimandasgupta.notemark.ui.common.DeviceLayoutType
import com.dhimandasgupta.notemark.ui.common.alignToSafeDrawing
import com.dhimandasgupta.notemark.ui.common.getDeviceLayoutType
import com.dhimandasgupta.notemark.ui.common.lifecycleAwareDebouncedClickable
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme
import com.dhimandasgupta.notemark.ui.designsystem.SafeIconButton
import com.dhimandasgupta.notemark.ui.extendedTabletLandscape
import com.dhimandasgupta.notemark.ui.extendedTabletPortrait
import com.dhimandasgupta.notemark.ui.mediumTabletLandscape
import com.dhimandasgupta.notemark.ui.mediumTabletPortrait
import com.dhimandasgupta.notemark.ui.phoneLandscape
import com.dhimandasgupta.notemark.ui.phonePortrait
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@Composable
fun EditNotePane(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    noteId: String,
    editNoteUiModel: EditNoteUiModel,
    editNoteAction: (EditNoteAction) -> Unit = {},
    onCloseClicked: () -> Unit = {}
) {
    val updatedEditNoteUiModel by rememberUpdatedState(newValue = editNoteUiModel)

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = updatedEditNoteUiModel.saved) {
        if (updatedEditNoteUiModel.saved == true) {
            focusManager.clearFocus()
            keyboardController?.hide()
            onCloseClicked()
        }
    }

    LaunchedEffect(key1 = noteId) {
        if (noteId.isNotEmpty()) {
            editNoteAction(EditNoteAction.LoadNote(uuid = noteId))
        }
    }

    val context = LocalActivity.current

    LaunchedEffect(key1 = updatedEditNoteUiModel.isReaderMode) {
        when (updatedEditNoteUiModel.isReaderMode) {
            true -> {
                context?.turnOnImmersiveMode()
                context?.lockToLandscape()
            }

            false -> {
                context?.turnOffImmersiveMode()
                context?.unlockOrientation()
            }
        }
    }

    val layoutType = getDeviceLayoutType(windowSizeClass)

    Column(
        modifier = modifier
            .background(color = colorScheme.surfaceContainerLowest)
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EditNoteToolbar(
            modifier = Modifier.wrapContentHeight(align = Alignment.Top),
            editEnabled = updatedEditNoteUiModel.editEnable,
            onCloseClicked = onCloseClicked,
            onCrossClicked = { editNoteAction(EditNoteAction.ModeChange(Mode.ViewMode)) },
            onSaveClicked = {
                focusManager.clearFocus()
                keyboardController?.hide()
                editNoteAction(EditNoteAction.Save)
            }
        )

        EditNoteBody(
            modifier = Modifier
                .fillMaxWidth(
                    fraction = when (layoutType) {
                        DeviceLayoutType.PHONE_PORTRAIT -> 1f
                        DeviceLayoutType.PHONE_LANDSCAPE -> 0.9f
                        else -> 0.85f
                    }
                )
                .fillMaxHeight(fraction = 1f)
                .lifecycleAwareDebouncedClickable(
                    onClick = {
                        if (updatedEditNoteUiModel.isReaderMode) {
                            editNoteAction(EditNoteAction.ModeChange(Mode.ViewMode))
                        }
                    }
                ),
            editNoteUiModel = updatedEditNoteUiModel,
            editNoteAction = editNoteAction
        )
    }
}

@Composable
private fun EditNoteToolbar(
    modifier: Modifier = Modifier,
    editEnabled: Boolean = false,
    onCloseClicked: () -> Unit = {},
    onCrossClicked: () -> Unit = {},
    onSaveClicked: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = editEnabled,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Row(
            modifier = modifier
                .background(color = colorScheme.surfaceContainerLowest)
                .fillMaxWidth()
                .padding(
                    start = WindowInsets.systemBars.union(insets = WindowInsets.displayCutout)
                        .asPaddingValues()
                        .calculateLeftPadding(LayoutDirection.Ltr) + 16.dp,
                    top = WindowInsets.systemBars.union(insets = WindowInsets.displayCutout)
                        .asPaddingValues()
                        .calculateTopPadding() + 16.dp,
                    end = WindowInsets.systemBars.union(insets = WindowInsets.displayCutout)
                        .asPaddingValues()
                        .calculateEndPadding(LayoutDirection.Ltr) + 16.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SafeIconButton(
                onClick = onCrossClicked
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_x),
                    contentDescription = "Close Note",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier
                )
            }

            Text(
                text = "Save Note".uppercase(),
                style = typography.titleSmall,
                color = colorScheme.primary,
                modifier = Modifier.lifecycleAwareDebouncedClickable { onSaveClicked() }
            )
        }
    }

    AnimatedVisibility(
        visible = !editEnabled,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Row(
            modifier = modifier
                .background(color = colorScheme.surfaceContainerLowest)
                .fillMaxWidth()
                .padding(
                    start = WindowInsets.systemBars.union(insets = WindowInsets.displayCutout)
                        .asPaddingValues()
                        .calculateLeftPadding(LayoutDirection.Ltr) + 16.dp,
                    top = WindowInsets.systemBars.union(insets = WindowInsets.displayCutout)
                        .asPaddingValues()
                        .calculateTopPadding() + 16.dp,
                    end = WindowInsets.systemBars.union(insets = WindowInsets.displayCutout)
                        .asPaddingValues()
                        .calculateEndPadding(LayoutDirection.Ltr) + 16.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.lifecycleAwareDebouncedClickable { onCloseClicked() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = "Settings",
                    tint = colorScheme.primary,
                    modifier = Modifier.requiredSize(size = 32.dp)
                )

                Text(
                    text = "All Notes".uppercase(),
                    style = typography.titleSmall,
                    color = colorScheme.primary
                )
            }
        }
    }
}

@OptIn(FlowPreview::class)
@Composable
private fun EditNoteBody(
    modifier: Modifier = Modifier,
    editNoteUiModel: EditNoteUiModel,
    editNoteAction: (EditNoteAction) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = Unit) { focusManager.clearFocus() }

    var title by remember(key1 = editNoteUiModel) { mutableStateOf(value = editNoteUiModel.title) }
    LaunchedEffect(key1 = Unit) {
        snapshotFlow { title }
            .debounce(timeoutMillis = 300)
            .collect { editNoteAction(EditNoteAction.UpdateTitle(title = title)) }
    }

    var body by remember(key1 = editNoteUiModel) { mutableStateOf(value = editNoteUiModel.content) }
    LaunchedEffect(key1 = Unit) {
        snapshotFlow { body }
            .debounce(timeoutMillis = 300)
            .collect { editNoteAction(EditNoteAction.UpdateContent(content = body)) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                bottom = WindowInsets.navigationBars.union(insets = WindowInsets.displayCutout)
                    .asPaddingValues()
                    .calculateBottomPadding() + 16.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(
                    start = WindowInsets.navigationBars.union(insets = WindowInsets.displayCutout)
                        .asPaddingValues()
                        .calculateLeftPadding(LayoutDirection.Ltr),
                    end = WindowInsets.navigationBars.union(insets = WindowInsets.displayCutout)
                        .asPaddingValues()
                        .calculateEndPadding(LayoutDirection.Ltr)
                )
                .windowInsetsPadding(insets = WindowInsets.ime)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                enabled = editNoteUiModel.editEnable,
                value = title,
                onValueChange = { value -> title = value },
                textStyle = typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(align = Alignment.Top)
                    .alignToSafeDrawing(),
                visualTransformation = VisualTransformation.None,
                placeholder = { Text(text = "Note title", style = typography.titleLarge) },
                colors = OutlinedTextFieldDefaults.colors().copy(
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface,
                    focusedContainerColor = colorScheme.surfaceContainerLowest,
                    unfocusedContainerColor = colorScheme.surfaceContainerLowest,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    disabledTextColor = colorScheme.onSurface,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Unspecified,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 1.dp)
                    .background(color = colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
            )

            AnimatedVisibility(
                visible = !editNoteUiModel.editEnable,
            ) {
                NoteDateTime(
                    modifier = Modifier,
                    dateCreated = editNoteUiModel.noteEntity?.createdAt ?: "",
                    lastEdited = editNoteUiModel.noteEntity?.lastEditedAt ?: "",
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 1.dp)
                    .background(color = colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
            )

            TextField(
                enabled = editNoteUiModel.editEnable,
                value = body,
                onValueChange = { value -> body = value },
                textStyle = typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(align = Alignment.Top)
                    .alignToSafeDrawing(),
                visualTransformation = VisualTransformation.None,
                placeholder = {
                    Text(
                        text = "Tap to enter note content",
                        style = typography.bodyLarge
                    )
                },
                colors = OutlinedTextFieldDefaults.colors().copy(
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface,
                    focusedContainerColor = colorScheme.surfaceContainerLowest,
                    unfocusedContainerColor = colorScheme.surfaceContainerLowest,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    disabledTextColor = colorScheme.onSurface,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Unspecified,
                    imeAction = ImeAction.Unspecified
                )
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = !editNoteUiModel.isReaderMode,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                EditAndViewMode(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onEditClicked = { editNoteAction(EditNoteAction.ModeChange(Mode.EditMode)) },
                    onViewClicked = { editNoteAction(EditNoteAction.ModeChange(Mode.ReaderMode)) }
                )
            }
        }
    }
}

@Composable
private fun NoteDateTime(
    modifier: Modifier = Modifier,
    dateCreated: String,
    lastEdited: String
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(weight = 1f)
        ) {
            Text(
                text = "Date Created",
                color = colorScheme.onSurfaceVariant,
                style = typography.bodyMedium
            )
            Text(
                text = dateCreated,
                style = typography.titleSmall
            )
        }

        Column(
            modifier = Modifier
                .weight(weight = 1f)
        ) {
            Text(
                text = "Last Edited",
                color = colorScheme.onSurfaceVariant,
                style = typography.bodyMedium
            )
            Text(
                text = lastEdited,
                style = typography.titleSmall
            )
        }
    }
}

@Composable
private fun EditAndViewMode(
    modifier: Modifier = Modifier,
    onEditClicked: () -> Unit,
    onViewClicked: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(shape = shapes.medium)
            .background(color = colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SafeIconButton(
            onClick = onEditClicked,
            modifier = Modifier
                .clip(shape = shapes.medium)
                .size(size = 56.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_edit),
                contentDescription = "Edit",
                tint = colorScheme.onSurfaceVariant,
            )
        }

        SafeIconButton(
            onClick = onViewClicked,
            modifier = Modifier
                .clip(shape = shapes.medium)
                .size(size = 56.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_view),
                contentDescription = "Edit",
                tint = colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhonePortraitPreview
@Composable
private fun PhonePortraitPreview() {
    NoteMarkTheme {
        EditNotePane(
            modifier = Modifier,
            windowSizeClass = phonePortrait,
            editNoteUiModel = defaultEditNoteUiModel,
            noteId = "1",
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhoneLandscapePreview
@Composable
private fun PhoneLandscapePreview() {
    NoteMarkTheme {
        EditNotePane(
            modifier = Modifier,
            windowSizeClass = phoneLandscape,
            editNoteUiModel = defaultEditNoteUiModel,
            noteId = "1",
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumPortraitPreview
@Composable
private fun TabletMediumPortraitPreview() {
    NoteMarkTheme {
        EditNotePane(
            modifier = Modifier,
            windowSizeClass = mediumTabletPortrait,
            editNoteUiModel = defaultEditNoteUiModel,
            noteId = "1",
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumLandscapePreview
@Composable
private fun TabletMediumLandscapePreview() {
    NoteMarkTheme {
        EditNotePane(
            modifier = Modifier,
            windowSizeClass = mediumTabletLandscape,
            editNoteUiModel = defaultEditNoteUiModel,
            noteId = "1",
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedPortraitPreview
@Composable
private fun TabletExpandedPortraitPreview() {
    NoteMarkTheme {
        EditNotePane(
            modifier = Modifier,
            windowSizeClass = extendedTabletPortrait,
            editNoteUiModel = defaultEditNoteUiModel,
            noteId = "1",
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedLandscapePreview
@Composable
private fun TabletExpandedLandscapePreview() {
    NoteMarkTheme {
        EditNotePane(
            modifier = Modifier,
            windowSizeClass = extendedTabletLandscape,
            editNoteUiModel = defaultEditNoteUiModel,
            noteId = "1",
        )
    }
}

private val defaultEditNoteUiModel = EditNoteUiModel(
    title = "Hello there, this is a the title of the Note",
    content = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
    noteEntity = NoteEntity(
        id = 1,
        title = "Hello there, this is a the title of the Note",
        content = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
        createdAt = "2025-06-29T19:18:24.369Z",
        lastEditedAt = "2025-06-30T21:58:37.634Z",
        uuid = "123e4567-e89b-12d3-a456-426614174000",
        synced = true,
        markAsDeleted = false
    )
)