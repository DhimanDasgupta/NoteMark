package com.dhimandasgupta.notemark.features.registration

import androidx.compose.runtime.retain.retain
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.common.navigation.EntryInstaller
import com.dhimandasgupta.notemark.common.navigation.RegistrationNavKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider

@ContributesIntoSet(AppScope::class)
@Inject
class RegistrationEntryInstaller(
  private val registrationPresenter: Provider<RegistrationPresenter>
) : EntryInstaller {
  override fun EntryProviderScope<NavKey>.install(backStack: NavBackStack<NavKey>) {
    entry<RegistrationNavKey> {
      val presenter: RegistrationPresenter = retain { registrationPresenter() }

      RegistrationEntry(
        registrationPresenter = presenter,
        navigateToLoginFromRegistration = { backStack.removeLastOrNull() },
      )
    }
  }
}
