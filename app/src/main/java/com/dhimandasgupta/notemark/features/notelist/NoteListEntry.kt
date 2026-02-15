package com.dhimandasgupta.notemark.features.notelist

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
fun NoteListEntry(
    modifier: Modifier = Modifier,
    navigateToAdd: () -> Unit,
    navigateToEdit: (String) -> Unit,
    navigateToSettings: () -> Unit
) {
    // Setup Presenter
    val noteListPresenter: NoteListPresenter = retain { get(clazz = NoteListPresenter::class.java) }
    var noteListUiModel by remember { mutableStateOf(value = NoteListUiModel.defaultOrEmpty) }
    val noteListAction by rememberUpdatedState(newValue = noteListPresenter::dispatchAction)

    // Setup scope and Lifecycle
    val scope = rememberCoroutineScope()
    LifecycleStartEffect(key1 = Unit) {
        if (scope.isActive) {
            scope.launchMolecule(mode = RecompositionMode.Immediate) {
                noteListUiModel = noteListPresenter.uiModel()
            }
        }
        onStopOrDispose {
            scope.cancel()
        }
    }

    // UI data, actions, navigation and events passing to UI
    NoteListPane(
        modifier = modifier,
        noteListUiModel = { noteListUiModel },
        noteListAction = { event -> noteListAction(event) },
        onNoteClicked = { uuid -> navigateToEdit(uuid) },
        onFabClicked = { navigateToAdd() },
        onSettingsClicked = { navigateToSettings() },
        onProfileClicked = {}
    )
}