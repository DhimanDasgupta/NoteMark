package com.dhimandasgupta.notemark.features.login

import androidx.compose.runtime.retain.retain
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.common.navigation.EntryInstaller
import com.dhimandasgupta.notemark.common.navigation.LoginNavKey
import com.dhimandasgupta.notemark.common.navigation.RegistrationNavKey
import com.dhimandasgupta.notemark.common.navigation.goToNoteListAfterLogin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
@Inject
class LoginEntryInstaller(private val loginPresenter: () -> LoginPresenter) : EntryInstaller {
  override fun EntryProviderScope<NavKey>.install(backStack: NavBackStack<NavKey>) {
    entry<LoginNavKey> {
      val presenter: LoginPresenter = retain { loginPresenter() }

      LoginEntry(
        loginPresenter = presenter,
        navigateToRegistration = { backStack.add(RegistrationNavKey) },
        navigateToAfterLogin = { backStack.goToNoteListAfterLogin() },
      )
    }
  }
}
