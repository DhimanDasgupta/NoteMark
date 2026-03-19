package com.dhimandasgupta.notemark.features.editnote

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
import com.dhimandasgupta.notemark.app.nav.NoteEditNavKey
import kotlinx.coroutines.isActive
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.get

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun EntryProviderScope<NavKey>.NoteEditEntryBuilder(
    modifier: Modifier,
    navigateUp: () -> Unit
) {
    entry<NoteEditNavKey>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) { noteEditNavKey ->
        val editNotePresenter: EditNotePresenter = retain {
            get(
                clazz = EditNotePresenter::class.java,
                parameters = { parametersOf(noteEditNavKey.noteId) }
            )
        }

        EditNoteEntry(
            modifier = modifier,
            editNotePresenter = editNotePresenter,
            navigateUp = navigateUp
        )
    }
}

@Composable
private fun EditNoteEntry(
    modifier: Modifier = Modifier,
    editNotePresenter: EditNotePresenter,
    navigateUp: () -> Unit
) {
    var editNoteUiModel by remember { mutableStateOf(value = EditNoteUiModel.defaultOrEmpty) }
    val editNoteAction by rememberUpdatedState(newValue = editNotePresenter::dispatchAction)

    LaunchedEffect(key1 = Unit) {
        if (isActive) {
            launchMolecule(mode = RecompositionMode.Immediate) {
                editNoteUiModel = editNotePresenter.uiModel()
            }
        }
    }

    // UI data, actions, navigation and events passing to UI
    EditNotePane(
        modifier = modifier,
        editNoteUiModel = { editNoteUiModel },
        editNoteAction = { event -> editNoteAction(event) },
        onCloseClicked = { navigateUp() }
    )
}