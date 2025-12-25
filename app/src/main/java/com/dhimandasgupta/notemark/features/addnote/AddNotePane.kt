package com.dhimandasgupta.notemark.features.addnote

import androidx.activity.compose.LocalActivity
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
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.dhimandasgupta.notemark.common.extensions.setDarkStatusBarIcons
import com.dhimandasgupta.notemark.ui.WindowSizePreviews
import com.dhimandasgupta.notemark.ui.common.DeviceLayoutType
import com.dhimandasgupta.notemark.ui.common.alignToSafeDrawing
import com.dhimandasgupta.notemark.ui.common.getDeviceLayoutType
import com.dhimandasgupta.notemark.ui.common.lifecycleAwareDebouncedClickable
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@Composable
fun AddNotePane(
    modifier: Modifier = Modifier,
    addNoteUiModel: () -> AddNoteUiModel,
    addNoteAction: (AddNoteAction) -> Unit = {},
    onBackClicked: () -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val context = LocalActivity.current
    SideEffect { context?.setDarkStatusBarIcons(true) }

    val updatedAddNoteUiModel by rememberUpdatedState(newValue = addNoteUiModel)

    LaunchedEffect(key1 = Unit) {
        snapshotFlow { updatedAddNoteUiModel().saved }
            .filter { isSaved -> isSaved == true }
            .collect { isSaved ->
                if (isSaved == true) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onBackClicked()
                }
            }
    }

    val layoutType = getDeviceLayoutType()

    Column(
        modifier = modifier
            .background(color = colorScheme.surfaceContainerLowest)
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AddNoteToolbar(
            modifier = Modifier.wrapContentHeight(align = Alignment.Top),
            onBackClicked = onBackClicked,
            onSaveClicked = {
                focusManager.clearFocus()
                keyboardController?.hide()
                addNoteAction(AddNoteAction.Save)
            }
        )

        AddNoteBody(
            modifier = Modifier
                .fillMaxWidth(
                    fraction = when (layoutType) {
                        DeviceLayoutType.PHONE_PORTRAIT -> 1f
                        DeviceLayoutType.PHONE_LANDSCAPE -> 0.9f
                        else -> 0.85f
                    }
                )
                .fillMaxHeight(fraction = 1f),
            addNoteUiModel = updatedAddNoteUiModel,
            addNoteAction = addNoteAction
        )
    }
}

@Composable
private fun AddNoteToolbar(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = {},
    onSaveClicked: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .background(color = colorScheme.surfaceContainerLowest)
            .fillMaxWidth()
            .padding(
                start = WindowInsets.systemBars.union(insets = WindowInsets.displayCutout)
                    .asPaddingValues()
                    .calculateLeftPadding(LayoutDirection.Ltr) + 0.dp,
                top = WindowInsets.systemBars.union(insets = WindowInsets.displayCutout)
                    .asPaddingValues()
                    .calculateTopPadding(),
                end = WindowInsets.systemBars.union(insets = WindowInsets.displayCutout)
                    .asPaddingValues()
                    .calculateEndPadding(LayoutDirection.Ltr) + 16.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.lifecycleAwareDebouncedClickable { onBackClicked() },
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

        Text(
            text = "Save Note".uppercase(),
            style = typography.titleSmall,
            color = colorScheme.primary,
            modifier = Modifier.lifecycleAwareDebouncedClickable { onSaveClicked() }
        )
    }
}

@OptIn(FlowPreview::class)
@Composable
private fun AddNoteBody(
    modifier: Modifier = Modifier,
    addNoteUiModel: () -> AddNoteUiModel,
    addNoteAction: (AddNoteAction) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = Unit) { focusManager.clearFocus() }

    var title by remember { mutableStateOf(value = addNoteUiModel().title) }
    var body by remember { mutableStateOf(value = addNoteUiModel().content) }

    LaunchedEffect(key1 = Unit) {
        launch {
            snapshotFlow { title }
                .debounce(timeoutMillis = 100)
                .collectLatest { addNoteAction(AddNoteAction.UpdateTitle(title = title)) }
        }

        launch {
            snapshotFlow { body }
                .debounce(timeoutMillis = 100)
                .collectLatest { addNoteAction(AddNoteAction.UpdateContent(content = body)) }
        }
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
                    .height(height = 1.dp)
                    .background(color = colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 1.dp)
                    .background(color = colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
            )

            TextField(
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
}

@WindowSizePreviews
@Composable
private fun AddNotePreview() {
    NoteMarkTheme {
        AddNotePane(
            modifier = Modifier,
            addNoteUiModel = { defaultAddNoteUiModel }
        )
    }
}

private val defaultAddNoteUiModel = AddNoteUiModel(
    title = "Hello there, this is a the title of the Note",
    content = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."
)