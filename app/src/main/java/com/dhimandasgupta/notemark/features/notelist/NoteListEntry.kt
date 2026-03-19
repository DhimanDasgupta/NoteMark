package com.dhimandasgupta.notemark.features.notelist

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
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
import com.dhimandasgupta.notemark.app.nav.NoteListNavKey
import kotlinx.coroutines.isActive
import org.koin.java.KoinJavaComponent.get

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun EntryProviderScope<NavKey>.NoteListEntryBuilder(
    modifier: Modifier,
    navigateToAdd: () -> Unit,
    navigateToEdit: (String) -> Unit,
    navigateToSettings: () -> Unit
) {
    entry<NoteListNavKey>(
        metadata = ListDetailSceneStrategy.listPane {
            NoNoteSelectedPane()
        }
    ) {
        val noteListPresenter: NoteListPresenter = retain { get(clazz = NoteListPresenter::class.java) }

        NoteListEntry(
            modifier = modifier,
            noteListPresenter = noteListPresenter,
            navigateToAdd = navigateToAdd,
            navigateToEdit = navigateToEdit,
            navigateToSettings = navigateToSettings
        )
    }
}

@Composable
private fun NoteListEntry(
    modifier: Modifier = Modifier,
    noteListPresenter: NoteListPresenter,
    navigateToAdd: () -> Unit,
    navigateToEdit: (String) -> Unit,
    navigateToSettings: () -> Unit
) {
    var noteListUiModel by remember { mutableStateOf(value = NoteListUiModel.defaultOrEmpty) }
    val noteListAction by rememberUpdatedState(newValue = noteListPresenter::dispatchAction)

    LaunchedEffect(key1 = Unit) {
        if (isActive) {
            launchMolecule(mode = RecompositionMode.Immediate) {
                noteListUiModel = noteListPresenter.uiModel()
            }
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