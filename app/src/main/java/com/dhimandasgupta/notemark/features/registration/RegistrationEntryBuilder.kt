package com.dhimandasgupta.notemark.features.registration

import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.app.di.LocalNoteMarkGraph
import com.dhimandasgupta.notemark.app.nav.RegistrationNavKey

@Composable
fun EntryProviderScope<NavKey>.RegistrationEntryBuilder(
  modifier: Modifier = Modifier,
  style: Style = Style,
  navigateToLoginFromRegistration: () -> Unit,
) {
  entry<RegistrationNavKey> {
    val graph = LocalNoteMarkGraph.current
    val registrationPresenter: RegistrationPresenter = retain {
      graph.registrationPresenter()
    }

    RegistrationEntry(
      modifier = modifier,
      style = style,
      registrationPresenter = registrationPresenter,
      navigateToLoginFromRegistration = navigateToLoginFromRegistration,
    )
  }
}
