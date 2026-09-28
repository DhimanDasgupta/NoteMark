package com.dhimandasgupta.notemark.features.editnote

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.dhimandasgupta.notemark.app.di.LocalNoteMarkGraph
import com.dhimandasgupta.notemark.app.nav.NoteEditNavKey
import com.dhimandasgupta.notemark.common.extensions.android.lockToLandscape
import com.dhimandasgupta.notemark.common.extensions.android.setDarkStatusBarIcons
import com.dhimandasgupta.notemark.common.extensions.android.turnOffImmersiveMode
import com.dhimandasgupta.notemark.common.extensions.android.turnOnImmersiveMode
import com.dhimandasgupta.notemark.common.extensions.android.unlockOrientation
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun EntryProviderScope<NavKey>.NoteEditEntryBuilder(
  modifier: Modifier,
  navigateUp: () -> Unit,
) {
  entry<NoteEditNavKey>(metadata = ListDetailSceneStrategy.detailPane()) { noteEditNavKey ->
    val noteId by rememberSaveable { mutableStateOf(noteEditNavKey.noteId) }
    val graph = LocalNoteMarkGraph.current
    val editNotePresenter: EditNotePresenter = retain {
      graph.editNotePresenterFactory().create(noteId)
    }

    EditNoteEntry(
      modifier = modifier,
      editNotePresenter = editNotePresenter,
      navigateUp = navigateUp,
    )
  }
}

@Composable
private fun EditNoteEntry(
  modifier: Modifier = Modifier,
  editNotePresenter: EditNotePresenter,
  navigateUp: () -> Unit,
) {
  val activity = LocalActivity.current ?: return
  SideEffect { activity.setDarkStatusBarIcons(true) }

  var editNoteUiModel by rememberSerializable {
    mutableStateOf(value = EditNoteUiModel.defaultOrEmpty)
  }
  val editNoteAction by rememberUpdatedState(newValue = editNotePresenter::dispatchAction)

  LaunchedEffect(key1 = Unit) {
    launchMolecule(mode = RecompositionMode.Immediate) {
        editNotePresenter.uiModel()
      }
      .collectLatest { model ->
        editNoteUiModel = model
      }
  }

  LaunchedEffect(key1 = Unit) {
    snapshotFlow { editNoteUiModel.isReaderMode }
      .collect { isReaderMode ->
        when (isReaderMode) {
          true -> {
            activity.turnOnImmersiveMode()
            activity.lockToLandscape()
          }

          false -> {
            activity.turnOffImmersiveMode()
            activity.unlockOrientation()
          }
        }
      }
  }

  // UI data, actions, navigation and events passing to UI
  EditNotePane(
    modifier = modifier,
    editNoteUiModel = { editNoteUiModel },
    editNoteAction = { action -> editNoteAction(action) },
    onCloseClicked = { navigateUp() },
  )
}
