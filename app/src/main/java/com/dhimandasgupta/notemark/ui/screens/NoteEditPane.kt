package com.dhimandasgupta.notemark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.dhimandasgupta.notemark.presenter.EditNoteUiModel
import com.dhimandasgupta.notemark.statemachine.EditNoteAction
import com.dhimandasgupta.notemark.ui.PhoneLandscapePreview
import com.dhimandasgupta.notemark.ui.PhonePortraitPreview
import com.dhimandasgupta.notemark.ui.TabletExpandedLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletExpandedPortraitPreview
import com.dhimandasgupta.notemark.ui.TabletMediumLandscapePreview
import com.dhimandasgupta.notemark.ui.TabletMediumPortraitPreview
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme
import com.dhimandasgupta.notemark.ui.extendedTabletLandscape
import com.dhimandasgupta.notemark.ui.extendedTabletPortrait
import com.dhimandasgupta.notemark.ui.mediumTabletLandscape
import com.dhimandasgupta.notemark.ui.mediumTabletPortrait
import com.dhimandasgupta.notemark.ui.phoneLandscape
import com.dhimandasgupta.notemark.ui.phonePortrait

@Composable
fun NoteEditPane(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    noteId: String = "",
    editNoteUiModel: EditNoteUiModel,
    editNoteAction: (EditNoteAction) -> Unit = {},
    onCloseClicked: () -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(editNoteUiModel.saved) {
        if (editNoteUiModel.saved == true) {
            focusManager.clearFocus()
            keyboardController?.hide()
            onCloseClicked()
        }
    }

    LaunchedEffect(key1 = noteId) {
        if (noteId.isNotEmpty()) {
            editNoteAction(EditNoteAction.LoadNote(noteId))
        }
    }

    Column(
        modifier = modifier
            .background(color = colorScheme.surfaceContainerLowest)
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top)
    ) {
        NoteEditToolbar(
            modifier = Modifier.wrapContentHeight(align = Alignment.Top),
            onCloseClicked = onCloseClicked,
            onSaveClicked = {
                focusManager.clearFocus()
                keyboardController?.hide()
                editNoteAction(EditNoteAction.Save)
            }
        )

        NoteEditBody(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(1f),
            titleText = editNoteUiModel.title,
            bodyText = editNoteUiModel.content,
            onTitleTextChanged =  { editNoteAction(EditNoteAction.UpdateTitle(it)) },
            onBodyTextChanged = { editNoteAction(EditNoteAction.UpdateContent(it)) }
        )
    }
}

@Composable
fun NoteEditToolbar(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onSaveClicked: () -> Unit = {}
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
                    .calculateEndPadding(LayoutDirection.Ltr) + 16.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onCloseClicked
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_x),
                contentDescription = "Close Note",
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier
            )
        }

        Text(
            text = "Save Note".uppercase(),
            style = typography.titleSmall,
            color = colorScheme.primary,
            modifier = Modifier.clickable { onSaveClicked() }
        )
    }
}

@Composable
fun NoteEditBody(
    modifier: Modifier = Modifier,
    titleText: String = "",
    bodyText: String = "",
    onTitleTextChanged: (String) -> Unit = {},
    onBodyTextChanged: (String) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) { focusManager.clearFocus() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(
                start = WindowInsets.navigationBars.union(WindowInsets.displayCutout)
                    .asPaddingValues()
                    .calculateLeftPadding(LayoutDirection.Ltr),
                end = WindowInsets.navigationBars.union(WindowInsets.displayCutout)
                    .asPaddingValues()
                    .calculateEndPadding(LayoutDirection.Ltr)
            )
            .windowInsetsPadding(WindowInsets.ime)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        TextField(
            value = titleText,
            onValueChange = { onTitleTextChanged(it) },
            textStyle = typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Top),
            visualTransformation = VisualTransformation.None,
            placeholder = { Text(text = "Note title", style = typography.titleLarge) },
            colors = OutlinedTextFieldDefaults.colors().copy(
                focusedTextColor = colorScheme.onSurface,
                unfocusedTextColor = colorScheme.onSurface,
                focusedContainerColor = colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
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
                .height(1.dp)
                .background(color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        )

        TextField(
            value = bodyText,
            onValueChange = { onBodyTextChanged(it) },
            textStyle = typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Top),
            visualTransformation = VisualTransformation.None,
            placeholder = { Text(text = "Tap to enter note content", style = typography.bodyLarge) },
            colors = OutlinedTextFieldDefaults.colors().copy(
                focusedTextColor = colorScheme.onSurface,
                unfocusedTextColor = colorScheme.onSurface,
                focusedContainerColor = colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Unspecified,
                imeAction = ImeAction.Unspecified
            )
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhonePortraitPreview
@Composable
private fun PhonePortraitPreview() {
    NoteMarkTheme {
        NoteEditPane(
            modifier = Modifier,
            windowSizeClass = phonePortrait,
            editNoteUiModel = defaultEditNoteUiModel
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PhoneLandscapePreview
@Composable
private fun PhoneLandscapePreview() {
    NoteMarkTheme {
        NoteEditPane(
            modifier = Modifier,
            windowSizeClass = phoneLandscape,
            editNoteUiModel = defaultEditNoteUiModel
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumPortraitPreview
@Composable
private fun TabletMediumPortraitPreview() {
    NoteMarkTheme {
        NoteEditPane(
            modifier = Modifier,
            windowSizeClass = mediumTabletPortrait,
            editNoteUiModel = defaultEditNoteUiModel
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletMediumLandscapePreview
@Composable
private fun TabletMediumLandscapePreview() {
    NoteMarkTheme {
        NoteEditPane(
            modifier = Modifier,
            windowSizeClass = mediumTabletLandscape,
            editNoteUiModel = defaultEditNoteUiModel
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedPortraitPreview
@Composable
private fun TabletExpandedPortraitPreview() {
    NoteMarkTheme {
        NoteEditPane(
            modifier = Modifier,
            windowSizeClass = extendedTabletPortrait,
            editNoteUiModel = defaultEditNoteUiModel
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@TabletExpandedLandscapePreview
@Composable
private fun TabletExpandedLandscapePreview() {
    NoteMarkTheme {
        NoteEditPane(
            modifier = Modifier,
            windowSizeClass = extendedTabletLandscape,
            editNoteUiModel = defaultEditNoteUiModel
        )
    }
}

private val defaultEditNoteUiModel = EditNoteUiModel(
    title = "Hello there, this is a the title of the Note",
    content = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
    noteEntity = null
)