package com.dhimandasgupta.notemark.features.launcher

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.dhimandasgupta.notemark.app.nav.LauncherNavKey
import kotlinx.coroutines.isActive
import org.koin.java.KoinJavaComponent.get

@Composable
fun EntryProviderScope<NavKey>.LauncherEntryBuilder(
    modifier: Modifier = Modifier,
    navigateAfterLogin: () -> Unit,
    navigateToLogin: () -> Unit
) {
    entry<LauncherNavKey> {
        val launcherPresenter: LauncherPresenter = retain { get(clazz = LauncherPresenter::class.java) }

        LauncherEntry(
            modifier = modifier,
            launcherPresenter = launcherPresenter,
            navigateAfterLogin = navigateAfterLogin,
            navigateToLogin = navigateToLogin
        )
    }
}

@Composable
private fun LauncherEntry(
    modifier: Modifier = Modifier,
    launcherPresenter: LauncherPresenter,
    navigateAfterLogin: () -> Unit,
    navigateToLogin: () -> Unit
) {
    val context = LocalActivity.current

    var launcherUiModel by remember { mutableStateOf(value = LauncherUiModel.defaultOrEmpty) }

    LaunchedEffect(key1 = Unit) {
        if (isActive) {
            launchMolecule(mode = RecompositionMode.Immediate) {
                launcherUiModel = launcherPresenter.uiModel()
            }
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