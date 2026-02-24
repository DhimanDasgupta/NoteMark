package com.dhimandasgupta.notemark.features.settings

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
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
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.dhimandasgupta.notemark.app.nav.SettingsNavKey
import com.dhimandasgupta.notemark.features.launcher.AppAction
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import org.koin.java.KoinJavaComponent.get

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun EntryProviderScope<NavKey>.SettingsEntryBuilder(
    modifier: Modifier,
    navigateToLauncherAfterLogout: () -> Unit,
    navigateUp: () -> Unit
) {
    entry<SettingsNavKey>(
        metadata = ListDetailSceneStrategy.extraPane()
    ) {
        SettingsEntry(
            modifier = modifier,
            navigateToLauncherAfterLogout = navigateToLauncherAfterLogout,
            navigateUp = navigateUp
        )
    }
}

@Composable
private fun SettingsEntry(
    modifier: Modifier = Modifier,
    navigateToLauncherAfterLogout: () -> Unit,
    navigateUp: () -> Unit
) {
    // Setup Presenter
    val settingsPresenter: SettingsPresenter = retain { get(clazz = SettingsPresenter::class.java) }
    var settingsUiModel by remember { mutableStateOf(value =  SettingsUiModel.defaultOrEmpty) }
    val settingsAction by rememberUpdatedState(newValue = settingsPresenter::dispatchAction)

    // Setup scope and Lifecycle
    val scope = rememberCoroutineScope()
    LifecycleStartEffect(key1 = Unit) {
        if (scope.isActive) {
            scope.launchMolecule(mode = RecompositionMode.Immediate) {
                settingsUiModel = settingsPresenter.uiModel()
            }
        }
        onStopOrDispose {
            scope.cancel()
        }
    }

    // UI data, actions, navigation and events passing to UI
    SettingsPane(
        modifier = modifier,
        settingsUiModel = { settingsUiModel },
        settingsAction = { event -> settingsAction(event) },
        onBackClicked = { navigateUp() },
        onLogoutSuccessful = { navigateToLauncherAfterLogout() },
        onDeleteNoteCheckChanged = {
            settingsAction(AppAction.DeleteLocalNotesOnLogout(deleteOnLogout = !settingsUiModel.deleteLocalNotesOnLogout))
        },
        onLogoutClicked = {
            settingsAction(AppAction.AppLogout)
        }
    )
}