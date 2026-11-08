package com.dhimandasgupta.notemark.features.settings

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.retain.retain
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.common.navigation.EntryInstaller
import com.dhimandasgupta.notemark.common.navigation.SettingsNavKey
import com.dhimandasgupta.notemark.common.navigation.removeSettingsKey
import com.dhimandasgupta.notemark.common.navigation.resetToLauncherAfterLogout
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider

@ContributesIntoSet(AppScope::class)
@Inject
class SettingsEntryInstaller(private val settingsPresenter: Provider<SettingsPresenter>) :
  EntryInstaller {
  @OptIn(ExperimentalMaterial3AdaptiveApi::class)
  override fun EntryProviderScope<NavKey>.install(backStack: NavBackStack<NavKey>) {
    entry<SettingsNavKey>(metadata = ListDetailSceneStrategy.extraPane()) {
      val presenter: SettingsPresenter = retain { settingsPresenter() }

      SettingsEntry(
        settingsPresenter = presenter,
        navigateToLauncherAfterLogout = { backStack.resetToLauncherAfterLogout() },
        navigateUp = { backStack.removeSettingsKey() },
      )
    }
  }
}
