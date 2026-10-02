package com.dhimandasgupta.notemark.features.registration

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
internal fun RegistrationEntry(
  modifier: Modifier = Modifier,
  style: Style = Style,
  registrationPresenter: RegistrationPresenter,
  navigateToLoginFromRegistration: () -> Unit,
) {
  val activity = LocalActivity.current ?: return
  SideEffect { activity.setDarkStatusBarIcons(false) }

  var registrationUiModel by rememberSerializable {
    mutableStateOf(value = RegistrationUiModel.defaultOrEmpty)
  }
  val registrationAction by rememberUpdatedState(newValue = registrationPresenter::dispatchAction)

  LaunchedEffect(key1 = Unit) {
    launchMolecule(mode = RecompositionMode.Immediate) {
        registrationPresenter.uiModel()
      }
      .collectLatest { model ->
        registrationUiModel = model
      }
  }

  // UI data, actions, navigation and events passing to UI
  RegistrationPane(
    modifier = modifier,
    style = style,
    registrationUiModel = { registrationUiModel },
    navigateToLogin = { navigateToLoginFromRegistration() },
    registrationAction = { action -> registrationAction(action) },
  )
}
