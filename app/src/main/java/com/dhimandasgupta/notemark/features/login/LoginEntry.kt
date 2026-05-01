package com.dhimandasgupta.notemark.features.login

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
import com.dhimandasgupta.notemark.app.nav.LoginNavKey
import kotlinx.coroutines.flow.collectLatest
import org.koin.java.KoinJavaComponent.get

@Composable
fun EntryProviderScope<NavKey>.LoginEntryBuilder(
    modifier: Modifier,
    navigateToRegistration: () -> Unit,
    navigateToAfterLogin: () -> Unit
) {
    entry<LoginNavKey> {
        val loginPresenter: LoginPresenter = retain { get(clazz = LoginPresenter::class.java) }

        LoginEntry(
            modifier = modifier,
            loginPresenter = loginPresenter,
            navigateToRegistration = navigateToRegistration,
            navigateToAfterLogin = navigateToAfterLogin
        )
    }
}

@Composable
private fun LoginEntry(
    modifier: Modifier = Modifier,
    loginPresenter: LoginPresenter,
    navigateToRegistration: () -> Unit,
    navigateToAfterLogin: () -> Unit
) {
    var loginUiModel by remember { mutableStateOf(value = LoginUiModel.defaultOrEmpty) }
    val loginEvents by rememberUpdatedState(newValue = loginPresenter::dispatchAction)

    LaunchedEffect(key1 = Unit) {
        launchMolecule(mode = RecompositionMode.Immediate) {
            loginPresenter.uiModel()
        }.collectLatest { model ->
            loginUiModel = model
        }
    }

    // UI data, actions, navigation and events passing to UI
    LoginPane(
        modifier = modifier,
        loginUiModel = { loginUiModel },
        loginAction = { action -> loginEvents(action) },
        navigateToRegistration = { navigateToRegistration() },
        navigateToAfterLogin = { navigateToAfterLogin() },
    )
}