package com.dhimandasgupta.notemark.features.registration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import org.koin.java.KoinJavaComponent.get

@Composable
fun RegistrationEntry(
    modifier: Modifier = Modifier,
    navigateToLoginFromRegistration: () -> Unit
) {
    // Setup Presenter
    val registrationPresenter: RegistrationPresenter = retain { get(clazz = RegistrationPresenter::class.java) }
    var registrationUiModel by remember { mutableStateOf(value = RegistrationUiModel.Empty) }
    val registrationAction by rememberUpdatedState(newValue = registrationPresenter::dispatchAction)

    // Setup scope and Lifecycle
    val scope = rememberCoroutineScope()
    LifecycleStartEffect(key1 = Unit) {
        if (scope.isActive) {
            scope.launchMolecule(mode = RecompositionMode.Immediate) {
                registrationUiModel = registrationPresenter.uiModel()
            }
        }
        onStopOrDispose {
            scope.cancel()
        }
    }

    // UI data, actions, navigation and events passing to UI
    RegistrationPane(
        modifier = modifier,
        registrationUiModel = { registrationUiModel },
        navigateToLogin = { navigateToLoginFromRegistration() },
        registrationAction = { event -> registrationAction(event) },
    )
}