package com.dhimandasgupta.notemark.features.addnote

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.retain.retain
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.common.navigation.EntryInstaller
import com.dhimandasgupta.notemark.common.navigation.NoteCreateNavKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider

@ContributesIntoSet(AppScope::class)
@Inject
class AddNoteEntryInstaller(private val addNotePresenter: Provider<AddNotePresenter>) :
  EntryInstaller {
  @OptIn(ExperimentalMaterial3AdaptiveApi::class)
  override fun EntryProviderScope<NavKey>.install(backStack: NavBackStack<NavKey>) {
    entry<NoteCreateNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
      val presenter: AddNotePresenter = retain { addNotePresenter() }

      AddNoteEntry(
        addNotePresenter = presenter,
        navigateUp = { backStack.removeLastOrNull() },
      )
    }
  }
}
