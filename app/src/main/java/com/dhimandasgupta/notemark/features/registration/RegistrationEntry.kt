package com.dhimandasgupta.notemark.features.registration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.dhimandasgupta.notemark.app.di.LocalNoteMarkGraph
import com.dhimandasgupta.notemark.app.nav.RegistrationNavKey
import kotlinx.coroutines.flow.collectLatest

@Composable
fun EntryProviderScope<NavKey>.RegistrationEntryBuilder(
  modifier: Modifier,
  navigateToLoginFromRegistration: () -> Unit,
) {
  entry<RegistrationNavKey> {
    val graph = LocalNoteMarkGraph.current
    val registrationPresenter: RegistrationPresenter = retain {
      graph.registrationPresenter()
    }

    RegistrationEntry(
      modifier = modifier,
      registrationPresenter = registrationPresenter,
      navigateToLoginFromRegistration = navigateToLoginFromRegistration,
    )
  }
}

@Composable
private fun RegistrationEntry(
  modifier: Modifier = Modifier,
  registrationPresenter: RegistrationPresenter,
  navigateToLoginFromRegistration: () -> Unit,
) {
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
    registrationUiModel = { registrationUiModel },
    navigateToLogin = { navigateToLoginFromRegistration() },
    registrationAction = { action -> registrationAction(action) },
  )
}
