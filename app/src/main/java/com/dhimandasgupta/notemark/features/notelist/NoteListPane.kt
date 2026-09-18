package com.dhimandasgupta.notemark.features.notelist

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.common.convertIsoToRelativeYearFormat
import com.dhimandasgupta.notemark.common.extensions.android.setDarkStatusBarIcons
import com.dhimandasgupta.notemark.common.extensions.kotlin.formatUserName
import com.dhimandasgupta.notemark.ui.WindowSizePreviews
import com.dhimandasgupta.notemark.ui.designsystem.LimitedText
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkFAB
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkToolbarButton
import com.dhimandasgupta.notemark.ui.designsystem.SafeIconButton
import com.dhimandasgupta.notemark.ui.designsystem.ThreeBouncingDots
import com.dhimandasgupta.notemark.ui.designsystem.bleedHorizontally
import java.util.Locale
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter

@Composable
internal fun NoteListPane(
  modifier: Modifier = Modifier,
  noteListUiModel: () -> NoteListUiModel,
  noteListAction: (NoteListAction) -> Unit = {},
  navigateToLauncherIfLoggedOut: () -> Unit = {},
  onNoteClicked: (String) -> Unit = {},
  onFabClicked: () -> Unit = {},
  onSettingsClicked: () -> Unit = {},
  onProfileClicked: () -> Unit = {},
) {
  val context = LocalActivity.current
  SideEffect { context?.setDarkStatusBarIcons(true) }

  val updateNoteListUiModel by rememberUpdatedState(newValue = noteListUiModel)
  var noteDeleteId by remember { mutableStateOf<String?>(value = null) }

  val userName by remember { derivedStateOf { updateNoteListUiModel().userName } }

  LaunchedEffect(key1 = userName) {
    delay(timeMillis = 100)
    if (updateNoteListUiModel().userName?.isEmpty() == true) {
      navigateToLauncherIfLoggedOut()
    }
  }

  Box(
    modifier = modifier.background(color = colorScheme.surfaceContainerLow).fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    NoteListValidPane(
      modifier = Modifier,
      userName = userName?.formatUserName() ?: "",
      noteListUiModel = updateNoteListUiModel,
      onNoteClicked = onNoteClicked,
      loadNotes = { noteListAction(NoteListAction.LoadNextNotes) },
      onNoteLongClicked = { id ->
        noteDeleteId = id
      },
      onFabClicked = onFabClicked,
      onSettingsClicked = onSettingsClicked,
      onProfileClicked = onProfileClicked,
    )

    // Dialog check
    if (!noteDeleteId.isNullOrEmpty()) {
      NoteDeleteDialog(
        modifier = Modifier,
        noteId = updateNoteListUiModel().noteLongClickedUuid,
        onDelete = { _ ->
          noteDeleteId?.let { id ->
            noteListAction(NoteListAction.NoteDelete(uuid = id))
          }
          noteDeleteId = null
        },
        onDismiss = {
          noteDeleteId = null
        },
      )
    }
  }
}

@Composable
private fun NoteListValidPane(
  modifier: Modifier = Modifier,
  userName: String,
  noteListUiModel: () -> NoteListUiModel,
  loadNotes: () -> Unit,
  onNoteClicked: (String) -> Unit = {},
  onNoteLongClicked: (String) -> Unit = {},
  onFabClicked: () -> Unit = {},
  onSettingsClicked: () -> Unit = {},
  onProfileClicked: () -> Unit = {},
) {
  when (noteListUiModel().noteEntities.isEmpty()) {
    true ->
      NoteListWithEmptyNotes(
        modifier = modifier,
        userName = userName,
        loading = noteListUiModel().loading,
        isConnected = noteListUiModel().isConnected,
        onFabClicked = onFabClicked,
        showSyncProgress = noteListUiModel().showSyncProgress,
        onSettingsClicked = onSettingsClicked,
        onProfileClicked = onProfileClicked,
      )

    else ->
      NoteListWithNotes(
        modifier = Modifier,
        userName = userName,
        loading = noteListUiModel().loading,
        noteListState = noteListUiModel,
        loadNotes = loadNotes,
        onNoteClicked = onNoteClicked,
        onNoteLongClicked = onNoteLongClicked,
        onFabClicked = onFabClicked,
        onSettingsClicked = onSettingsClicked,
        onProfileClicked = onProfileClicked,
      )
  }
}

@Composable
private fun NoteListWithEmptyNotes(
  modifier: Modifier = Modifier,
  userName: String,
  loading: Boolean,
  isConnected: Boolean,
  showSyncProgress: Boolean,
  onFabClicked: () -> Unit,
  onSettingsClicked: () -> Unit,
  onProfileClicked: () -> Unit,
) {
  NoNotes(
    modifier = modifier,
    userName = userName,
    loading = loading,
    isConnected = isConnected,
    showSyncProgress = showSyncProgress,
    onFabClicked = onFabClicked,
    onSettingsClicked = onSettingsClicked,
    onProfileClicked = onProfileClicked,
  )
}

@Composable
private fun NoteListWithNotes(
  modifier: Modifier = Modifier,
  userName: String,
  loading: Boolean,
  noteListState: () -> NoteListUiModel,
  loadNotes: () -> Unit,
  onNoteClicked: (String) -> Unit = {},
  onNoteLongClicked: (String) -> Unit = {},
  onFabClicked: () -> Unit = {},
  onSettingsClicked: () -> Unit = {},
  onProfileClicked: () -> Unit = {},
) {
  LoadingPane(
    modifier = Modifier,
    showLoading = loading,
  )
  if (loading) return

  var columnCount by remember { mutableIntStateOf(value = 2) }
  var maxLength by remember { mutableIntStateOf(value = 150) }

  val density = LocalDensity.current

  val scrollState = rememberLazyStaggeredGridState()

  val currentLoadNotes by rememberUpdatedState(newValue = loadNotes)
  LaunchedEffect(key1 = scrollState) {
    snapshotFlow {
        val layoutInfo = scrollState.layoutInfo
        layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1
      }
      .filter { reachedBottom -> reachedBottom }
      .collect { currentLoadNotes() }
  }

  Box(
    modifier =
      modifier.fillMaxSize().onSizeChanged { intSize ->
        val widthInDp = with(density) { intSize.width.toDp() }

        columnCount =
          when {
            widthInDp < 600.dp -> 2
            widthInDp < 840.dp -> 3
            else -> 4
          }

        maxLength =
          when {
            widthInDp < 600.dp -> 150
            widthInDp < 840.dp -> 250
            else -> 300
          }
      }
  ) {
    NoteGrid(
      modifier = Modifier.fillMaxSize(),
      columnCount = columnCount,
      maxLength = maxLength,
      state = scrollState,
      noteListUiModel = noteListState,
      userName = userName,
      onNoteClicked = onNoteClicked,
      onNoteLongClicked = onNoteLongClicked,
      onSettingsClicked = onSettingsClicked,
      onProfileClicked = onProfileClicked,
    )

    NoteListFab(
      scrollState = scrollState,
      onFabClicked = onFabClicked,
    )
  }
}

@Composable
private fun NoteListFab(
  scrollState: LazyStaggeredGridState,
  onFabClicked: () -> Unit,
) {
  val visibility = remember { Animatable(initialValue = 1f) }

  LaunchedEffect(key1 = scrollState) {
    snapshotFlow { scrollState.firstVisibleItemIndex == 0 && !scrollState.isScrollInProgress }
      .collectLatest { shouldShow ->
        visibility.animateTo(targetValue = if (shouldShow) 1f else 0f)
      }
  }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .windowInsetsPadding(
          insets = WindowInsets.navigationBars.union(insets = WindowInsets.displayCutout)
        ),
    contentAlignment = Alignment.BottomEnd,
  ) {
    NoteMarkFAB(
      modifier =
        Modifier.padding(all = 16.dp).graphicsLayer {
          val progress = visibility.value
          alpha = progress
          translationY = (1f - progress) * size.height / 2f
        },
      onClick = { if (visibility.targetValue == 1f) onFabClicked() },
    )
  }
}

@Composable
fun LoadingPane(
  modifier: Modifier = Modifier,
  showLoading: Boolean,
) {
  AnimatedVisibility(
    visible = showLoading,
    enter = scaleIn() + fadeIn(),
    exit = scaleOut() + fadeOut(),
  ) {
    Box(
      modifier = modifier.fillMaxSize(),
      contentAlignment = Alignment.Center,
    ) {
      ThreeBouncingDots(
        modifier = Modifier.padding(all = 16.dp).wrapContentSize(),
        dotColor1 = colorResource(id = R.color.splash_blue).copy(alpha = 0.5f),
        dotColor2 = colorResource(id = R.color.splash_blue).copy(alpha = 0.75f),
        dotColor3 = colorResource(id = R.color.splash_blue).copy(alpha = 1.0f),
      )
    }
  }
}

@Composable
private fun NoteListPaneToolbar(
  modifier: Modifier = Modifier,
  toolbarTitle: String,
  userName: String,
  isConnected: Boolean,
  onSettingsClicked: () -> Unit,
  onProfileClicked: () -> Unit,
) {
  Row(
    modifier =
      modifier
        .background(color = colorScheme.surfaceContainerLowest)
        .fillMaxWidth()
        .padding(
          top =
            WindowInsets.systemBars
              .union(WindowInsets.displayCutout)
              .asPaddingValues()
              .calculateTopPadding(),
          start =
            WindowInsets.navigationBars
              .union(WindowInsets.displayCutout)
              .asPaddingValues()
              .calculateLeftPadding(LayoutDirection.Ltr) + 16.dp,
          end =
            WindowInsets.navigationBars
              .union(WindowInsets.displayCutout)
              .asPaddingValues()
              .calculateRightPadding(LayoutDirection.Ltr) + 16.dp,
        ),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = toolbarTitle,
      style = typography.titleMedium,
      modifier = Modifier.weight(weight = 1f),
    )

    SafeIconButton(onClick = onSettingsClicked) {
      Icon(
        painter = painterResource(id = R.drawable.ic_settings),
        contentDescription = "Settings",
        modifier = Modifier.requiredSize(size = 48.dp),
      )
    }

    Spacer(modifier = Modifier.width(width = 16.dp))

    NoteMarkToolbarButton(
      title = userName,
      isConnected = isConnected,
      onClick = onProfileClicked,
    )
  }
}

@Composable
private fun NoNotes(
  modifier: Modifier = Modifier,
  toolbarTitle: String = "NoteMark",
  userName: String = "",
  loading: Boolean,
  isConnected: Boolean,
  showSyncProgress: Boolean,
  onSettingsClicked: () -> Unit,
  onProfileClicked: () -> Unit,
  onFabClicked: () -> Unit = {},
) {
  Box(modifier = modifier.fillMaxSize()) {
    NoteListPaneToolbar(
      modifier = Modifier,
      toolbarTitle = toolbarTitle,
      userName = userName,
      isConnected = isConnected,
      onSettingsClicked = onSettingsClicked,
      onProfileClicked = onProfileClicked,
    )

    when (showSyncProgress || loading) {
      true ->
        Box(modifier = Modifier.align(Alignment.Center).wrapContentSize().padding(all = 16.dp)) {
          ThreeBouncingDots(
            modifier = Modifier.padding(all = 16.dp).wrapContentSize(),
            dotColor1 = colorResource(id = R.color.splash_blue).copy(alpha = 0.5f),
            dotColor2 = colorResource(id = R.color.splash_blue).copy(alpha = 0.75f),
            dotColor3 = colorResource(id = R.color.splash_blue).copy(alpha = 1.0f),
          )
        }

      else ->
        Text(
          text = "You’ve got an empty board, \n let’s place your first note on it!",
          style = typography.titleSmall,
          color = colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier =
            modifier
              .fillMaxWidth()
              .padding(
                paddingValues =
                  WindowInsets.displayCutout
                    .union(insets = WindowInsets.statusBars)
                    .union(insets = WindowInsets.navigationBars)
                    .asPaddingValues()
              )
              .padding(vertical = 96.dp, horizontal = 32.dp),
        )
    }

    NoteMarkFAB(
      modifier = Modifier.padding(all = 16.dp).align(Alignment.BottomEnd),
      onClick = onFabClicked,
    )
  }
}

@Composable
private fun NoteGrid(
  modifier: Modifier = Modifier,
  columnCount: Int,
  maxLength: Int,
  state: LazyStaggeredGridState,
  noteListUiModel: () -> NoteListUiModel,
  userName: String,
  onNoteClicked: (String) -> Unit = {},
  onNoteLongClicked: (String) -> Unit = {},
  onSettingsClicked: () -> Unit = {},
  onProfileClicked: () -> Unit = {},
) {
  val configuration = LocalConfiguration.current
  val locale =
    remember(key1 = configuration) {
      configuration.locales.getFirstMatch(arrayOf("en")) ?: configuration.locales.get(0)
    }

  val insets =
    WindowInsets.navigationBars.union(insets = WindowInsets.displayCutout).asPaddingValues()
  val startPadding = insets.calculateLeftPadding(LayoutDirection.Ltr) + 8.dp
  val endPadding = insets.calculateEndPadding(LayoutDirection.Ltr) + 8.dp

  LazyVerticalStaggeredGrid(
    state = state,
    columns = StaggeredGridCells.Fixed(count = columnCount),
    contentPadding =
      PaddingValues(
        start = startPadding,
        end = endPadding,
        bottom = insets.calculateBottomPadding() + 8.dp,
      ),
    verticalItemSpacing = 8.dp,
    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
    modifier = modifier.fillMaxSize(),
  ) {
    item(
      span = StaggeredGridItemSpan.FullLine,
      key = "toolbar",
      contentType = "toolbar",
    ) {
      NoteListPaneToolbar(
        modifier = Modifier.bleedHorizontally(start = startPadding, end = endPadding),
        toolbarTitle = "NoteMark",
        userName = userName,
        isConnected = noteListUiModel().isConnected,
        onSettingsClicked = onSettingsClicked,
        onProfileClicked = onProfileClicked,
      )
    }

    if (noteListUiModel().showSyncProgress) {
      item(
        span = StaggeredGridItemSpan.FullLine,
        key = "sync_progress",
        contentType = "sync_progress",
      ) {
        ThreeBouncingDots(
          modifier = Modifier.padding(all = 16.dp).wrapContentSize(),
          dotColor1 = colorResource(id = R.color.splash_blue).copy(alpha = 0.5f),
          dotColor2 = colorResource(id = R.color.splash_blue).copy(alpha = 0.75f),
          dotColor3 = colorResource(id = R.color.splash_blue).copy(alpha = 1.0f),
        )
      }
    }

    items(
      items = noteListUiModel().noteEntities,
      key = { note -> note.id },
      contentType = { "notes" },
    ) { noteEntity ->
      val appearance = remember { Animatable(0f) }
      LaunchedEffect(Unit) {
        appearance.animateTo(
          targetValue = 1f,
          animationSpec =
            spring(
              stiffness = Spring.StiffnessLow,
              dampingRatio = Spring.DampingRatioLowBouncy,
            ),
        )
      }
      NoteItem(
        modifier =
          Modifier.animateItem(
              fadeInSpec = null,
              placementSpec =
                spring(
                  stiffness = Spring.StiffnessLow,
                  dampingRatio = Spring.DampingRatioLowBouncy,
                ),
              fadeOutSpec =
                spring(
                  stiffness = Spring.StiffnessVeryLow,
                  dampingRatio = Spring.DampingRatioLowBouncy,
                ),
            )
            .graphicsLayer {
              val progress = appearance.value
              val scale = 0.5f + 0.5f * progress
              scaleX = scale
              scaleY = scale
              translationY = (1f - progress) * NoteItemAppearOffset.toPx()
            },
        note = noteEntity,
        maxLength = maxLength,
        locale = locale,
        onNoteClicked = onNoteClicked,
        onNoteLongClicked = onNoteLongClicked,
      )
    }
  }
}

/** How far below its final position a note starts when it appears in the grid. */
private val NoteItemAppearOffset = 100.dp

@Composable
private fun NoteItem(
  modifier: Modifier = Modifier,
  note: NoteEntityUiModel,
  maxLength: Int,
  locale: Locale,
  onNoteClicked: (String) -> Unit = {},
  onNoteLongClicked: (String) -> Unit = {},
) {
  Column(
    modifier =
      modifier
        .clip(shape = shapes.medium)
        .background(color = colorScheme.surfaceContainerLowest)
        .combinedClickable(
          onClick = { onNoteClicked(note.uuid) },
          onLongClick = { onNoteLongClicked(note.uuid) },
        )
        .innerShadow(
          shape = shapes.medium,
          shadow =
            Shadow(
              radius = 12.dp,
              color = colorScheme.primary,
              spread = 4.dp,
              alpha = 0.4f,
            ),
        )
        .padding(all = 16.dp)
  ) {
    val lastEditedLabel =
      remember(key1 = note.lastEditedAt, key2 = locale) {
        convertIsoToRelativeYearFormat(
          locale = locale,
          isoOffsetDateTimeString = note.lastEditedAt,
        )
      }

    Text(
      text = lastEditedLabel,
      style = typography.bodyMedium,
      color = colorScheme.primary,
    )

    Spacer(modifier = Modifier.height(height = 8.dp))

    LimitedText(
      fullText = note.title,
      style = typography.titleMedium,
      color = colorScheme.onSurface,
      targetCharacterCount = maxLength,
    )

    Spacer(modifier = Modifier.height(height = 4.dp))

    LimitedText(
      fullText = note.content,
      style = typography.bodySmall,
      color = colorScheme.onSurfaceVariant,
      targetCharacterCount = maxLength,
    )
  }
}

@WindowSizePreviews
@Composable
private fun NoteListPanePreview(
  @PreviewParameter(NoteListUiModelPreviewParameterProvider::class) noteListUiModel: NoteListUiModel
) {
  NoteMarkTheme {
    NoteListPane(
      modifier = Modifier,
      noteListUiModel = { noteListUiModel },
    )
  }
}

private class NoteListUiModelPreviewParameterProvider : PreviewParameterProvider<NoteListUiModel> {
  override val values: Sequence<NoteListUiModel>
    get() =
      sequenceOf(
        NoteListUiModel(
          userName = "Dhiman",
          loading = false,
          noteEntities =
            persistentListOf(
              NoteEntityUiModel(
                id = 0,
                title = "This is a title for the Note\nThis is a title for the Note",
                content = "This is content For the Note\nThis is content For the Note",
                createdAt = "19th Apr",
                lastEditedAt = "20th Apr",
                uuid = "e1ed931c-5cd1-4c87-8b13-83ab25f1307d",
                synced = true,
                markAsDeleted = false,
              ),
              NoteEntityUiModel(
                id = 1,
                title = "This is a title for the Note",
                content = "This is content For the Note",
                createdAt = "19th Apr",
                lastEditedAt = "20th Apr",
                uuid = "e1ed931c-5cd1-4c87-8b13-83ab25f1307d",
                synced = true,
                markAsDeleted = false,
              ),
              NoteEntityUiModel(
                id = 2,
                title = "This is a title for the Note\nThis is a title for the Note",
                content = "This is content For the Note\nThis is content For the Note",
                createdAt = "19th Apr",
                lastEditedAt = "20th Apr",
                uuid = "e1ed931c-5cd1-4c87-8b13-83ab25f1307d",
                synced = true,
                markAsDeleted = false,
              ),
              NoteEntityUiModel(
                id = 3,
                title = "This is a title for the Note",
                content = "This is content For the Note",
                createdAt = "19th Apr",
                lastEditedAt = "20th Apr",
                uuid = "e1ed931c-5cd1-4c87-8b13-83ab25f1307d",
                synced = true,
                markAsDeleted = false,
              ),
              NoteEntityUiModel(
                id = 4,
                title = "This is a title for the Note\nThis is a title for the Note",
                content = "This is content For the Note\nThis is content For the Note",
                createdAt = "19th Apr",
                lastEditedAt = "20th Apr",
                uuid = "e1ed931c-5cd1-4c87-8b13-83ab25f1307d",
                synced = true,
                markAsDeleted = false,
              ),
              NoteEntityUiModel(
                id = 5,
                title = "This is a title for the Note",
                content = "This is content For the Note",
                createdAt = "19th Apr",
                lastEditedAt = "20th Apr",
                uuid = "e1ed931c-5cd1-4c87-8b13-83ab25f1307d",
                synced = true,
                markAsDeleted = false,
              ),
            ),
        ),
        NoteListUiModel(
          userName = "Dhiman",
          loading = false,
          noteEntities = persistentListOf(),
        ),
        NoteListUiModel(
          userName = "Dhiman",
          loading = true,
          isConnected = true,
          noteEntities =
            persistentListOf(
              NoteEntityUiModel(
                id = 0,
                title = "This is a title for the Note\nThis is a title for the Note",
                content = "This is content For the Note\nThis is content For the Note",
                createdAt = "19th Apr",
                lastEditedAt = "20th Apr",
                uuid = "e1ed931c-5cd1-4c87-8b13-83ab25f1307d",
                synced = true,
                markAsDeleted = false,
              ),
              NoteEntityUiModel(
                id = 1,
                title = "This is a title for the Note",
                content = "This is content For the Note",
                createdAt = "19th Apr",
                lastEditedAt = "20th Apr",
                uuid = "e1ed931c-5cd1-4c87-8b13-83ab25f1307d",
                synced = true,
                markAsDeleted = false,
              ),
              NoteEntityUiModel(
                id = 2,
                title = "This is a title for the Note\nThis is a title for the Note",
                content = "This is content For the Note\nThis is content For the Note",
                createdAt = "19th Apr",
                lastEditedAt = "20th Apr",
                uuid = "e1ed931c-5cd1-4c87-8b13-83ab25f1307d",
                synced = true,
                markAsDeleted = false,
              ),
              NoteEntityUiModel(
                id = 3,
                title = "This is a title for the Note",
                content = "This is content For the Note",
                createdAt = "19th Apr",
                lastEditedAt = "20th Apr",
                uuid = "e1ed931c-5cd1-4c87-8b13-83ab25f1307d",
                synced = true,
                markAsDeleted = false,
              ),
              NoteEntityUiModel(
                id = 4,
                title = "This is a title for the Note\nThis is a title for the Note",
                content = "This is content For the Note\nThis is content For the Note",
                createdAt = "19th Apr",
                lastEditedAt = "20th Apr",
                uuid = "e1ed931c-5cd1-4c87-8b13-83ab25f1307d",
                synced = true,
                markAsDeleted = false,
              ),
              NoteEntityUiModel(
                id = 5,
                title = "This is a title for the Note",
                content = "This is content For the Note",
                createdAt = "19th Apr",
                lastEditedAt = "20th Apr",
                uuid = "e1ed931c-5cd1-4c87-8b13-83ab25f1307d",
                synced = true,
                markAsDeleted = false,
              ),
            ),
        ),
      )
}
