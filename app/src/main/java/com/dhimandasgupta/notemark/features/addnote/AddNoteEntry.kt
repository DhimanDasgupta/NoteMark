package com.dhimandasgupta.notemark.features.addnote

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
import com.dhimandasgupta.notemark.app.nav.NoteCreateNavKey
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import org.koin.java.KoinJavaComponent.get

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun EntryProviderScope<NavKey>.NoteCreateEntryBuilder(
    modifier: Modifier,
    navigateUp: () -> Unit
) {
    entry<NoteCreateNavKey>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) {
        AddNoteEntry(
            modifier = modifier,
            navigateUp = navigateUp
        )
    }
}

@Composable
private fun AddNoteEntry(
    modifier: Modifier = Modifier,
    addNotePresenter: AddNotePresenter = get(clazz = AddNotePresenter::class.java),
    navigateUp: () -> Unit
) {
    var addNoteUiModel by remember { mutableStateOf(value = AddNoteUiModel.defaultOrEmpty) }
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