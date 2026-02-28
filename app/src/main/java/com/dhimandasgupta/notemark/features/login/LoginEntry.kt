package com.dhimandasgupta.notemark.features.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.dhimandasgupta.notemark.app.nav.LoginNavKey
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import org.koin.java.KoinJavaComponent.get

@Composable
fun EntryProviderScope<NavKey>.LoginEntryBuilder(
    modifier: Modifier,
    navigateToRegistration: () -> Unit,
    navigateToAfterLogin: () -> Unit
) {
    entry<LoginNavKey> {
        LoginEntry(
            modifier = modifier,
            navigateToRegistration = navigateToRegistration,
            navigateToAfterLogin = navigateToAfterLogin
        )
    }
}

@Composable
private fun LoginEntry(
    modifier: Modifier = Modifier,
    loginPresenter: LoginPresenter = get(clazz = LoginPresenter::class.java),
    navigateToRegistration: () -> Unit,
    navigateToAfterLogin: () -> Unit
) {
    var loginUiModel by remember { mutableStateOf(value = LoginUiModel.defaultOrEmpty) }
    val loginEvents by rememberUpdatedState(newValue = loginPresenter::dispatchAction)

    // Setup scope and Lifecycle
    val scope = rememberCoroutineScope()
    LifecycleStartEffect(key1 = Unit) {
        if (scope.isActive) {
            scope.launchMolecule(mode = RecompositionMode.Immediate) {
                loginUiModel = loginPresenter.uiModel()
            }
        }
        onStopOrDispose {
            scope.cancel()
        }
    }

    // UI data, actions, navigation and events passing to UI
    LoginPane(
        modifier = modifier,
        loginUiModel = { loginUiModel },
        loginAction = { event -> loginEvents(event) },
        navigateToRegistration = { navigateToRegistration() },
        navigateToAfterLogin = { navigateToAfterLogin() },
    )
}