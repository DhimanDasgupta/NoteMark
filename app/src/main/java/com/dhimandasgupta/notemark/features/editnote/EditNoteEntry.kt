package com.dhimandasgupta.notemark.features.editnote

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
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.get

@Composable
fun EditNoteEntry(
    modifier: Modifier = Modifier,
    argument: String,
    navigateUp: () -> Unit
) {
    // Setup Presenter
    val editNotePresenter: EditNotePresenter = retain {
        get(
            clazz = EditNotePresenter::class.java,
            parameters = { parametersOf(argument) }
        )
    }
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