package com.dhimandasgupta.notemark.features.notelist

import androidx.compose.foundation.style.Style
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.app.di.LocalNoteMarkGraph
import com.dhimandasgupta.notemark.app.nav.NoteListNavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun EntryProviderScope<NavKey>.NoteListEntryBuilder(
  modifier: Modifier = Modifier,
  style: Style = Style,
  navigateToLauncherIfLoggedOut: () -> Unit,
  navigateToAdd: () -> Unit,
  navigateToEdit: (String) -> Unit,
  navigateToSettings: () -> Unit,
) {
  entry<NoteListNavKey>(
    metadata =
      ListDetailSceneStrategy.listPane {
        NoNoteSelectedPane()
      }
  ) {
    val graph = LocalNoteMarkGraph.current
    val noteListPresenter: NoteListPresenter = retain { graph.noteListPresenter() }

    NoteListEntry(
      modifier = modifier,
      style = style,
      noteListPresenter = noteListPresenter,
      navigateToLauncherIfLoggedOut = navigateToLauncherIfLoggedOut,
      navigateToAdd = navigateToAdd,
      navigateToEdit = navigateToEdit,
      navigateToSettings = navigateToSettings,
    )
  }
}
