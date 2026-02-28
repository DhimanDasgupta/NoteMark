package com.dhimandasgupta.notemark.features.editnote

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
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
import com.dhimandasgupta.notemark.app.nav.NoteEditNavKey
import kotlinx.coroutines.cancel
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
        EditNoteEntry(
            modifier = modifier,
            argument = noteEditNavKey.noteId,
            navigateUp = navigateUp
        )
    }
}

@Composable
private fun EditNoteEntry(
    modifier: Modifier = Modifier,
    argument: String,
    editNotePresenter: EditNotePresenter = get(
        clazz = EditNotePresenter::class.java,
        parameters = { parametersOf(argument) }
    ),
    navigateUp: () -> Unit
) {
    var editNoteUiModel by remember { mutableStateOf(value = EditNoteUiModel.defaultOrEmpty) }
    val editNoteAction by rememberUpdatedState(newValue = editNotePresenter::dispatchAction)

    // Setup scope and Lifecycle
    val scope = rememberCoroutineScope()
    LifecycleStartEffect(key1 = argument) {
        if (scope.isActive) {
            scope.launchMolecule(mode = RecompositionMode.Immediate) {
                editNoteUiModel = editNotePresenter.uiModel()
            }
        }
        onStopOrDispose {
            scope.cancel()
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