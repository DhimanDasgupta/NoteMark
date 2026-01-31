package com.dhimandasgupta.notemark.features.launcher

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
fun LauncherEntry(
    modifier: Modifier = Modifier,
    navigateAfterLogin: () -> Unit,
    navigateToLogin: () -> Unit
) {
    val context = LocalActivity.current

    // Setup Presenter
    val launcherPresenter: LauncherPresenter = retain { get(clazz = LauncherPresenter::class.java) }
    var launcherUiModel by remember { mutableStateOf(value = LauncherUiModel.Empty) }

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