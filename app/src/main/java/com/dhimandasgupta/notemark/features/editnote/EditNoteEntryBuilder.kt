package com.dhimandasgupta.notemark.features.editnote

import androidx.compose.foundation.style.Style
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.app.di.LocalNoteMarkGraph
import com.dhimandasgupta.notemark.app.nav.NoteEditNavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun EntryProviderScope<NavKey>.NoteEditEntryBuilder(
  modifier: Modifier = Modifier,
  style: Style = Style,
  navigateUp: () -> Unit,
) {
  entry<NoteEditNavKey>(metadata = ListDetailSceneStrategy.detailPane()) { noteEditNavKey ->
    val noteId by rememberSaveable { mutableStateOf(noteEditNavKey.noteId) }
    val graph = LocalNoteMarkGraph.current
    val editNotePresenter: EditNotePresenter = retain {
      graph.editNotePresenterFactory().create(noteId)
    }

    EditNoteEntry(
      modifier = modifier,
      style = style,
      editNotePresenter = editNotePresenter,
      navigateUp = navigateUp,
    )
  }
}
