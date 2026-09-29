package com.dhimandasgupta.notemark.features.settings

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
import com.dhimandasgupta.notemark.features.launcher.AppAction
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun SettingsEntry(
  modifier: Modifier = Modifier,
  settingsPresenter: SettingsPresenter,
  navigateToLauncherAfterLogout: () -> Unit,
  navigateUp: () -> Unit,
) {
  val activity = LocalActivity.current ?: return
  SideEffect { activity.setDarkStatusBarIcons(true) }

  var settingsUiModel by rememberSerializable {
    mutableStateOf(value = SettingsUiModel.defaultOrEmpty)
  }
  val settingsAction by rememberUpdatedState(newValue = settingsPresenter::dispatchAction)

  LaunchedEffect(key1 = Unit) {
    launchMolecule(mode = RecompositionMode.Immediate) {
        settingsPresenter.uiModel()
      }
      .collectLatest { model ->
        settingsUiModel = model
      }
  }

  // UI data, actions, navigation and events passing to UI
  SettingsPane(
    modifier = modifier,
    settingsUiModel = { settingsUiModel },
    settingsAction = { action -> settingsAction(action) },
    onBackClicked = { navigateUp() },
    onLogoutSuccessful = { navigateToLauncherAfterLogout() },
    onDeleteNoteCheckChanged = {
      settingsAction(
        AppAction.DeleteLocalNotesOnLogout(
          deleteOnLogout = !settingsUiModel.deleteLocalNotesOnLogout
        )
      )
    },
    onLogoutClicked = {
      settingsAction(AppAction.AppLogout)
    },
  )
}
