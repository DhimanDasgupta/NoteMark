package com.dhimandasgupta.notemark.features.addnote

import androidx.compose.foundation.style.Style
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.app.di.LocalNoteMarkGraph
import com.dhimandasgupta.notemark.app.nav.NoteCreateNavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun EntryProviderScope<NavKey>.NoteCreateEntryBuilder(
  modifier: Modifier = Modifier,
  style: Style = Style,
  navigateUp: () -> Unit,
) {
  entry<NoteCreateNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
    val graph = LocalNoteMarkGraph.current
    val addNotePresenter: AddNotePresenter = retain { graph.addNotePresenter() }

    AddNoteEntry(
      modifier = modifier,
      style = style,
      addNotePresenter = addNotePresenter,
      navigateUp = navigateUp,
    )
  }
}
