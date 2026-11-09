package com.dhimandasgupta.notemark.features.launcher

import androidx.compose.runtime.retain.retain
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.common.navigation.EntryInstaller
import com.dhimandasgupta.notemark.common.navigation.LauncherNavKey
import com.dhimandasgupta.notemark.common.navigation.LoginNavKey
import com.dhimandasgupta.notemark.common.navigation.goToNoteListAfterLogin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
@Inject
class LauncherEntryInstaller(private val launcherPresenter: () -> LauncherPresenter) :
  EntryInstaller {
  override fun EntryProviderScope<NavKey>.install(backStack: NavBackStack<NavKey>) {
    entry<LauncherNavKey> {
      val presenter: LauncherPresenter = retain { launcherPresenter() }

      LauncherEntry(
        launcherPresenter = presenter,
        navigateAfterLogin = { backStack.goToNoteListAfterLogin() },
        navigateToLogin = { backStack.add(LoginNavKey) },
      )
    }
  }
}
