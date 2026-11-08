package com.dhimandasgupta.notemark.features.editnote

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.common.navigation.EntryInstaller
import com.dhimandasgupta.notemark.common.navigation.NoteEditNavKey
import com.dhimandasgupta.notemark.common.navigation.clearNoteEditNavKeys
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
@Inject
class EditNoteEntryInstaller(private val editNotePresenterFactory: EditNotePresenter.Factory) :
  EntryInstaller {
  @OptIn(ExperimentalMaterial3AdaptiveApi::class)
  override fun EntryProviderScope<NavKey>.install(backStack: NavBackStack<NavKey>) {
    entry<NoteEditNavKey>(metadata = ListDetailSceneStrategy.detailPane()) { noteEditNavKey ->
      val noteId by rememberSaveable { mutableStateOf(noteEditNavKey.noteId) }
      val presenter: EditNotePresenter = retain { editNotePresenterFactory.create(noteId) }

      EditNoteEntry(
        editNotePresenter = presenter,
        navigateUp = { backStack.clearNoteEditNavKeys() },
      )
    }
  }
}
