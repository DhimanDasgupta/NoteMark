package com.dhimandasgupta.notemark.features.registration

import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.common.navigation.RegistrationNavKey

@Composable
fun EntryProviderScope<NavKey>.RegistrationEntryBuilder(
  modifier: Modifier = Modifier,
  style: Style = Style,
  registrationPresenter: () -> RegistrationPresenter,
  navigateToLoginFromRegistration: () -> Unit,
) {
  entry<RegistrationNavKey> {
    val presenter: RegistrationPresenter = retain { registrationPresenter() }

    RegistrationEntry(
      modifier = modifier,
      style = style,
      registrationPresenter = presenter,
      navigateToLoginFromRegistration = navigateToLoginFromRegistration,
    )
  }
}
