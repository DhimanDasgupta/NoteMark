package com.dhimandasgupta.notemark.features.login

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
internal fun LoginEntry(
  modifier: Modifier = Modifier,
  style: Style = Style,
  loginPresenter: LoginPresenter,
  navigateToRegistration: () -> Unit,
  navigateToAfterLogin: () -> Unit,
) {
  val activity = LocalActivity.current ?: return
  SideEffect { activity.setDarkStatusBarIcons(false) }

  var loginUiModel by rememberSerializable { mutableStateOf(value = LoginUiModel.defaultOrEmpty) }
  val loginEvents by rememberUpdatedState(newValue = loginPresenter::dispatchAction)

  LaunchedEffect(key1 = Unit) {
    launchMolecule(mode = RecompositionMode.Immediate) {
        loginPresenter.uiModel()
      }
      .collectLatest { model ->
        loginUiModel = model
      }
  }

  // UI data, actions, navigation and events passing to UI
  LoginPane(
    modifier = modifier,
    style = style,
    loginUiModel = { loginUiModel },
    loginAction = { action -> loginEvents(action) },
    navigateToRegistration = { navigateToRegistration() },
    navigateToAfterLogin = { navigateToAfterLogin() },
  )
}
