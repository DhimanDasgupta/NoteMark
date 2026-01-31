package com.dhimandasgupta.notemark.features.addnote

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
fun AddNoteEntry(
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit
) {
    // Setup Presenter
    val addNotePresenter:AddNotePresenter = retain { get(clazz = AddNotePresenter::class.java) }
    var addNoteUiModel by remember { mutableStateOf(value = AddNoteUiModel.Empty) }
    val addNoteAction by rememberUpdatedState(newValue = addNotePresenter::dispatchAction)

    // Setup scope and Lifecycle
    val scope = rememberCoroutineScope()
    LifecycleStartEffect(key1 = Unit) {
        if (scope.isActive) {
            scope.launchMolecule(mode = RecompositionMode.Immediate) {
                addNoteUiModel = addNotePresenter.uiModel()
            }
        }
        onStopOrDispose {
            scope.cancel()
        }
    }

    // UI data, actions, navigation and events passing to UI
    AddNotePane(
        modifier = modifier,
        addNoteUiModel = { addNoteUiModel },
        addNoteAction = { event -> addNoteAction(event) },
        onBackClicked = { navigateUp() }
    )
}