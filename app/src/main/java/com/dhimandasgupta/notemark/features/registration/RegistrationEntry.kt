package com.dhimandasgupta.notemark.features.registration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.dhimandasgupta.notemark.app.nav.RegistrationNavKey
import org.koin.java.KoinJavaComponent.get

@Composable
fun EntryProviderScope<NavKey>.RegistrationEntryBuilder(
    modifier: Modifier,
    navigateToLoginFromRegistration: () -> Unit
) {
    entry<RegistrationNavKey> {
        val registrationPresenter: RegistrationPresenter = retain { get(clazz = RegistrationPresenter::class.java) }

        RegistrationEntry(
            modifier = modifier,
            registrationPresenter = registrationPresenter,
            navigateToLoginFromRegistration = navigateToLoginFromRegistration
        )
    }
}

@Composable
private fun RegistrationEntry(
    modifier: Modifier = Modifier,
    registrationPresenter: RegistrationPresenter,
    navigateToLoginFromRegistration: () -> Unit
) {
    var registrationUiModel by remember { mutableStateOf(value = RegistrationUiModel.defaultOrEmpty) }
    val registrationAction by rememberUpdatedState(newValue = registrationPresenter::dispatchAction)

    LaunchedEffect(key1 = Unit) {
        launchMolecule(mode = RecompositionMode.Immediate) {
            registrationUiModel = registrationPresenter.uiModel()
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