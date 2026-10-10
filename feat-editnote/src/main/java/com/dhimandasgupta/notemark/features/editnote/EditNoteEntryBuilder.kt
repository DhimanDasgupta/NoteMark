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
import com.dhimandasgupta.notemark.common.navigation.NoteEditNavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun EntryProviderScope<NavKey>.NoteEditEntryBuilder(
  modifier: Modifier = Modifier,
  style: Style = Style,
  editNotePresenter: (noteId: String) -> EditNotePresenter,
  navigateUp: () -> Unit,
) {
  entry<NoteEditNavKey>(metadata = ListDetailSceneStrategy.detailPane()) { noteEditNavKey ->
    val noteId by rememberSaveable { mutableStateOf(noteEditNavKey.noteId) }
    val presenter: EditNotePresenter = retain { editNotePresenter(noteId) }

    EditNoteEntry(
      modifier = modifier,
      style = style,
      editNotePresenter = presenter,
      navigateUp = navigateUp,
    )
  }
}
