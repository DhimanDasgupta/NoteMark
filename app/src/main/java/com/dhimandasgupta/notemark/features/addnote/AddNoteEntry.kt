package com.dhimandasgupta.notemark.features.addnote

import androidx.activity.compose.LocalActivity
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
internal fun AddNoteEntry(
  modifier: Modifier = Modifier,
  addNotePresenter: AddNotePresenter,
  navigateUp: () -> Unit,
) {
  val activity = LocalActivity.current ?: return
  SideEffect { activity.setDarkStatusBarIcons(true) }

  var addNoteUiModel by rememberSerializable {
    mutableStateOf(value = AddNoteUiModel.defaultOrEmpty)
  }
  val addNoteAction by rememberUpdatedState(newValue = addNotePresenter::dispatchAction)

  LaunchedEffect(key1 = Unit) {
    launchMolecule(mode = RecompositionMode.Immediate) {
        addNotePresenter.uiModel()
      }
      .collectLatest { model ->
        addNoteUiModel = model
      }
  }

  // UI data, actions, navigation and events passing to UI
  AddNotePane(
    modifier = modifier,
    addNoteUiModel = { addNoteUiModel },
    addNoteAction = { action -> addNoteAction(action) },
    onBackClicked = { navigateUp() },
  )
}
