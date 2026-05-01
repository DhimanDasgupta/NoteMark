package com.dhimandasgupta.notemark.features.addnote

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
import com.dhimandasgupta.notemark.app.nav.NoteCreateNavKey
import kotlinx.coroutines.flow.collectLatest
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
        val addNotePresenter: AddNotePresenter = retain { get(clazz = AddNotePresenter::class.java) }

        AddNoteEntry(
            modifier = modifier,
            addNotePresenter = addNotePresenter,
            navigateUp = navigateUp
        )
    }
}

@Composable
private fun AddNoteEntry(
    modifier: Modifier = Modifier,
    addNotePresenter: AddNotePresenter,
    navigateUp: () -> Unit
) {
    var addNoteUiModel by remember { mutableStateOf(value = AddNoteUiModel.defaultOrEmpty) }
    val addNoteAction by rememberUpdatedState(newValue = addNotePresenter::dispatchAction)

    LaunchedEffect(key1 = Unit) {
        launchMolecule(mode = RecompositionMode.Immediate) {
            addNotePresenter.uiModel()
        }.collectLatest { model ->
            addNoteUiModel = model
        }
    }

    // UI data, actions, navigation and events passing to UI
    AddNotePane(
        modifier = modifier,
        addNoteUiModel = { addNoteUiModel },
        addNoteAction = { action -> addNoteAction(action) },
        onBackClicked = { navigateUp() }
    )
}