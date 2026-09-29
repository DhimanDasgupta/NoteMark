package com.dhimandasgupta.notemark.features.launcher

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.dhimandasgupta.notemark.common.extensions.android.setForcedDarkStatusBarIcons
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun LauncherEntry(
  modifier: Modifier = Modifier,
  launcherPresenter: LauncherPresenter,
  navigateAfterLogin: () -> Unit,
  navigateToLogin: () -> Unit,
) {
  val activity = LocalActivity.current ?: return
  SideEffect { activity.setForcedDarkStatusBarIcons(true) }

  var launcherUiModel by rememberSerializable {
    mutableStateOf(value = LauncherUiModel.defaultOrEmpty)
  }

  LaunchedEffect(key1 = Unit) {
    launchMolecule(mode = RecompositionMode.Immediate) {
        launcherPresenter.uiModel()
      }
      .collectLatest { model ->
        launcherUiModel = model
      }
  }

  // UI data, actions, navigation and events passing to UI
  LauncherPane(
    modifier = modifier,
    launcherUiModel = { launcherUiModel },
    navigateToAfterLogin = {
      if (launcherUiModel.loggedInUser == null) {
        Toast.makeText(
            activity,
            "Oops!!! Please login first to get started",
            Toast.LENGTH_LONG,
          )
          .show()
        return@LauncherPane
      }
      navigateAfterLogin()
    },
    navigateToLogin = { navigateToLogin() },
    navigateToList = { navigateAfterLogin() },
  )
}
