package com.dhimandasgupta.notemark.features.notelist

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.dhimandasgupta.notemark.common.extensions.android.setDarkStatusBarIcons
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun NoteListEntry(
  modifier: Modifier = Modifier,
  style: Style = Style,
  noteListPresenter: NoteListPresenter,
  navigateToLauncherIfLoggedOut: () -> Unit,
  navigateToAdd: () -> Unit,
  navigateToEdit: (String) -> Unit,
  navigateToSettings: () -> Unit,
) {
  val activity = LocalActivity.current ?: return
  SideEffect { activity.setDarkStatusBarIcons(true) }

  var noteListUiModel by rememberSerializable {
    mutableStateOf(value = NoteListUiModel.defaultOrEmpty)
  }
  val noteListAction by rememberUpdatedState(newValue = noteListPresenter::dispatchAction)
  val appAction by rememberUpdatedState(newValue = noteListPresenter::dispatchAppAction)

  LaunchedEffect(key1 = Unit) {
    launchMolecule(mode = RecompositionMode.Immediate) {
        noteListPresenter.uiModel()
      }
      .collectLatest { model ->
        noteListUiModel = model
      }
  }

  // UI data, actions, navigation and events passing to UI
  NoteListPane(
    modifier = modifier,
    style = style,
    noteListUiModel = { noteListUiModel },
    noteListAction = { action -> noteListAction(action) },
    appAction = { action -> appAction(action) },
    onNoteClicked = { uuid -> navigateToEdit(uuid) },
    navigateToLauncherIfLoggedOut = { navigateToLauncherIfLoggedOut() },
    onFabClicked = { navigateToAdd() },
    onSettingsClicked = { navigateToSettings() },
    onProfileClicked = {},
  )
}
