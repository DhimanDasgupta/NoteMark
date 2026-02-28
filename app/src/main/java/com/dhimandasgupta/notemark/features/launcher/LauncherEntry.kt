package com.dhimandasgupta.notemark.features.launcher

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.dhimandasgupta.notemark.app.nav.LauncherNavKey
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import org.koin.java.KoinJavaComponent.get

@Composable
fun EntryProviderScope<NavKey>.LauncherEntryBuilder(
    modifier: Modifier = Modifier,
    navigateAfterLogin: () -> Unit,
    navigateToLogin: () -> Unit
) {
    entry<LauncherNavKey> {
        LauncherEntry(
            modifier = modifier,
            navigateAfterLogin = navigateAfterLogin,
            navigateToLogin = navigateToLogin
        )
    }
}

@Composable
private fun LauncherEntry(
    modifier: Modifier = Modifier,
    launcherPresenter: LauncherPresenter = get(clazz = LauncherPresenter::class.java),
    navigateAfterLogin: () -> Unit,
    navigateToLogin: () -> Unit
) {
    val context = LocalActivity.current

    var launcherUiModel by remember { mutableStateOf(value = LauncherUiModel.defaultOrEmpty) }

    // Setup scope and Lifecycle
    val scope = rememberCoroutineScope()
    LifecycleStartEffect(key1 = Unit) {
        if (scope.isActive) {
            scope.launchMolecule(mode = RecompositionMode.Immediate) {
                launcherUiModel = launcherPresenter.uiModel()
            }
        }
        onStopOrDispose {
            scope.cancel()
        }
    }

    // UI data, actions, navigation and events passing to UI
    LauncherPane(
        modifier = modifier,
        launcherUiModel = { launcherUiModel },
        navigateToAfterLogin = {
            if (launcherUiModel.loggedInUser == null) {
                Toast.makeText(
                    context,
                    "Oops!!! Please login first to get started",
                    Toast.LENGTH_LONG
                ).show()
                return@LauncherPane
            }
            navigateAfterLogin()
        },
        navigateToLogin = { navigateToLogin() },
        navigateToList = { navigateAfterLogin() }
    )
}