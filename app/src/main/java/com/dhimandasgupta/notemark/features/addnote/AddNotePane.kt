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
import androidx.compose.material3.TextFieldColors
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.common.extensions.android.setDarkStatusBarIcons
import com.dhimandasgupta.notemark.common.extensions.compose.DeviceLayoutType
import com.dhimandasgupta.notemark.common.extensions.compose.alignToSafeDrawing
import com.dhimandasgupta.notemark.common.extensions.compose.getDeviceLayoutType
import com.dhimandasgupta.notemark.common.extensions.compose.lifecycleAwareDebouncedClickable
import com.dhimandasgupta.notemark.ui.WindowSizePreviews
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter

@Composable
internal fun AddNotePane(
  modifier: Modifier = Modifier,
  addNoteUiModel: () -> AddNoteUiModel,
  addNoteAction: (AddNoteAction) -> Unit = {},
  onBackClicked: () -> Unit = {},
) {
  val keyboardController = LocalSoftwareKeyboardController.current
  val focusManager = LocalFocusManager.current

  val context = LocalActivity.current ?: return
  SideEffect { context.setDarkStatusBarIcons(true) }

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
    modifier =
      modifier
        .background(color = colorScheme.surfaceContainerLowest)
        .fillMaxWidth()
        .wrapContentHeight(align = Alignment.Top),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    AddNoteToolbar(
      modifier = Modifier.wrapContentHeight(align = Alignment.Top),
      onBackClicked = onBackClicked,
      onSaveClicked = {
        focusManager.clearFocus()
        keyboardController?.hide()
        addNoteAction(AddNoteAction.Save)
      },
    )

    AddNoteBody(
      modifier =
        Modifier.fillMaxWidth(
            fraction =
              when (layoutType) {
                DeviceLayoutType.PHONE_PORTRAIT -> 1f
                DeviceLayoutType.PHONE_LANDSCAPE -> 0.9f
                else -> 0.85f
              }
          )
          .fillMaxHeight(fraction = 1f),
      addNoteUiModel = updatedAddNoteUiModel,
      addNoteAction = addNoteAction,
    )
  }
}

@Composable
private fun AddNoteToolbar(
  modifier: Modifier = Modifier,
  onBackClicked: () -> Unit = {},
  onSaveClicked: () -> Unit = {},
) {
  Row(
    modifier =
      modifier
        .background(color = colorScheme.surfaceContainerLowest)
        .fillMaxWidth()
        .padding(
          start =
            WindowInsets.systemBars
              .union(insets = WindowInsets.displayCutout)
              .asPaddingValues()
              .calculateLeftPadding(LayoutDirection.Ltr) + 0.dp,
          top =
            WindowInsets.systemBars
              .union(insets = WindowInsets.displayCutout)
              .asPaddingValues()
              .calculateTopPadding(),
          end =
            WindowInsets.systemBars
              .union(insets = WindowInsets.displayCutout)
              .asPaddingValues()
              .calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
        ),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      modifier = Modifier.lifecycleAwareDebouncedClickable { onBackClicked() },
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        painter = painterResource(id = R.drawable.ic_back_arrow),
        contentDescription = "Settings",
        tint = colorScheme.primary,
        modifier = Modifier.requiredSize(size = 32.dp),
      )

      Text(
        text = "All Notes".uppercase(),
        style = typography.titleSmall,
        color = colorScheme.primary,
      )
    }

    Text(
      text = "Save Note".uppercase(),
      style = typography.titleSmall,
      color = colorScheme.primary,
      modifier = Modifier.lifecycleAwareDebouncedClickable { onSaveClicked() },
    )
  }
}

@Composable
private fun AddNoteBody(
  modifier: Modifier = Modifier,
  addNoteUiModel: () -> AddNoteUiModel,
  addNoteAction: (AddNoteAction) -> Unit = {},
) {
  val focusManager = LocalFocusManager.current
  val scrollState = rememberScrollState()

  LaunchedEffect(key1 = Unit) { focusManager.clearFocus() }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .padding(
          bottom =
            WindowInsets.navigationBars
              .union(insets = WindowInsets.displayCutout)
              .asPaddingValues()
              .calculateBottomPadding() + 16.dp
        ),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        modifier
          .fillMaxWidth()
          .verticalScroll(scrollState)
          .windowInsetsPadding(insets = WindowInsets.ime)
          .padding(vertical = 16.dp),
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // Each field owns its text state, so a keystroke only recomposes that field rather than
      // this whole body (both fields, both dividers and their colors).
      AddNoteTitleField(
        modifier =
          Modifier.fillMaxWidth().wrapContentHeight(align = Alignment.Top).alignToSafeDrawing(),
        initialTitle = addNoteUiModel().title,
        onTitleChanged = { title -> addNoteAction(AddNoteAction.UpdateTitle(title = title)) },
        onNextClicked = { focusManager.moveFocus(FocusDirection.Next) },
      )

      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(height = 1.dp)
            .background(color = colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
      )

      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(height = 1.dp)
            .background(color = colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
      )

      AddNoteContentField(
        modifier =
          Modifier.fillMaxWidth().wrapContentHeight(align = Alignment.Top).alignToSafeDrawing(),
        initialContent = addNoteUiModel().content,
        onContentChanged = { content ->
          addNoteAction(AddNoteAction.UpdateContent(content = content))
        },
      )
    }
  }
}

@OptIn(FlowPreview::class)
@Composable
private fun AddNoteTitleField(
  modifier: Modifier = Modifier,
  initialTitle: String,
  onTitleChanged: (String) -> Unit,
  onNextClicked: () -> Unit,
) {
  var title by remember { mutableStateOf(value = initialTitle) }

  val updatedOnTitleChanged by rememberUpdatedState(newValue = onTitleChanged)
  LaunchedEffect(key1 = Unit) {
    snapshotFlow { title }
      .debounce(timeoutMillis = 100)
      .collectLatest { updatedOnTitleChanged(title) }
  }

  TextField(
    value = title,
    onValueChange = { value -> title = value },
    textStyle = typography.titleLarge,
    modifier = modifier,
    visualTransformation = VisualTransformation.None,
    placeholder = { Text(text = "Note title", style = typography.titleLarge) },
    colors = addNoteTextFieldColors(),
    keyboardOptions =
      KeyboardOptions(
        keyboardType = KeyboardType.Unspecified,
        imeAction = ImeAction.Next,
      ),
    keyboardActions = KeyboardActions(onNext = { onNextClicked() }),
  )
}

@OptIn(FlowPreview::class)
@Composable
private fun AddNoteContentField(
  modifier: Modifier = Modifier,
  initialContent: String,
  onContentChanged: (String) -> Unit,
) {
  var body by remember { mutableStateOf(value = initialContent) }

  val updatedOnContentChanged by rememberUpdatedState(newValue = onContentChanged)
  LaunchedEffect(key1 = Unit) {
    snapshotFlow { body }
      .debounce(timeoutMillis = 100)
      .collectLatest { updatedOnContentChanged(body) }
  }

  TextField(
    value = body,
    onValueChange = { value -> body = value },
    textStyle = typography.bodyLarge,
    modifier = modifier,
    visualTransformation = VisualTransformation.None,
    placeholder = {
      Text(
        text = "Tap to enter note content",
        style = typography.bodyLarge,
      )
    },
    colors = addNoteTextFieldColors(),
    keyboardOptions =
      KeyboardOptions(
        keyboardType = KeyboardType.Unspecified,
        imeAction = ImeAction.Unspecified,
      ),
  )
}

/**
 * Remembered per color scheme so a keystroke does not rebuild the colors on every recomposition.
 */
@Composable
private fun addNoteTextFieldColors(): TextFieldColors {
  val colorScheme = colorScheme
  val defaults = OutlinedTextFieldDefaults.colors()
  return remember(colorScheme, defaults) {
    defaults.copy(
      focusedTextColor = colorScheme.onSurface,
      unfocusedTextColor = colorScheme.onSurface,
      focusedContainerColor = colorScheme.surfaceContainerLowest,
      unfocusedContainerColor = Color.Transparent,
      focusedIndicatorColor = Color.Transparent,
      unfocusedIndicatorColor = Color.Transparent,
      disabledIndicatorColor = Color.Transparent,
      errorIndicatorColor = Color.Transparent,
    )
  }
}

@WindowSizePreviews
@Composable
private fun AddNotePreview(
  @PreviewParameter(provider = AddNoteUiModelPreviewProvider::class)
  defaultAddNoteUiModel: AddNoteUiModel
) {
  NoteMarkTheme {
    AddNotePane(
      modifier = Modifier,
      addNoteUiModel = { defaultAddNoteUiModel },
    )
  }
}

private class AddNoteUiModelPreviewProvider : PreviewParameterProvider<AddNoteUiModel> {
  override val values: Sequence<AddNoteUiModel>
    get() =
      sequenceOf(
        element =
          AddNoteUiModel(
            title = "Hello there, this is a the title of the Note",
            content =
              "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
          )
      )
}
