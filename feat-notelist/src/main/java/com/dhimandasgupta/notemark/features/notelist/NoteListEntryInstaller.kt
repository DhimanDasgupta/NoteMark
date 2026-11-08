package com.dhimandasgupta.notemark.features.notelist

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.retain.retain
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.common.navigation.EntryInstaller
import com.dhimandasgupta.notemark.common.navigation.NoteCreateNavKey
import com.dhimandasgupta.notemark.common.navigation.NoteEditNavKey
import com.dhimandasgupta.notemark.common.navigation.NoteListNavKey
import com.dhimandasgupta.notemark.common.navigation.SettingsNavKey
import com.dhimandasgupta.notemark.common.navigation.clearNoteEditNavKeys
import com.dhimandasgupta.notemark.common.navigation.isSettingsOpen
import com.dhimandasgupta.notemark.common.navigation.resetToLauncherAfterLogout
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider

@ContributesIntoSet(AppScope::class)
@Inject
class NoteListEntryInstaller(private val noteListPresenter: Provider<NoteListPresenter>) :
  EntryInstaller {
  @OptIn(ExperimentalMaterial3AdaptiveApi::class)
  override fun EntryProviderScope<NavKey>.install(backStack: NavBackStack<NavKey>) {
    entry<NoteListNavKey>(
      metadata =
        ListDetailSceneStrategy.listPane {
          NoNoteSelectedPane()
        }
    ) {
      val presenter: NoteListPresenter = retain { noteListPresenter() }

      NoteListEntry(
        noteListPresenter = presenter,
        navigateToLauncherIfLoggedOut = { backStack.resetToLauncherAfterLogout() },
        navigateToAdd = { backStack.add(NoteCreateNavKey) },
        navigateToEdit = { uuid ->
          backStack.apply {
            clearNoteEditNavKeys()
            add(NoteEditNavKey(uuid))
          }
        },
        navigateToSettings = {
          if (!backStack.isSettingsOpen()) {
            backStack.add(SettingsNavKey)
          }
        },
      )
    }
  }
}
