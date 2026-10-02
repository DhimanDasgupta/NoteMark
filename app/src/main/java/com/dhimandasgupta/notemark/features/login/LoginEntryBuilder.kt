package com.dhimandasgupta.notemark.features.login

import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.app.di.LocalNoteMarkGraph
import com.dhimandasgupta.notemark.app.nav.LoginNavKey

@Composable
fun EntryProviderScope<NavKey>.LoginEntryBuilder(
  modifier: Modifier = Modifier,
  style: Style = Style,
  navigateToRegistration: () -> Unit,
  navigateToAfterLogin: () -> Unit,
) {
  entry<LoginNavKey> {
    val graph = LocalNoteMarkGraph.current
    val loginPresenter: LoginPresenter = retain { graph.loginPresenter() }

    LoginEntry(
      modifier = modifier,
      style = style,
      loginPresenter = loginPresenter,
      navigateToRegistration = navigateToRegistration,
      navigateToAfterLogin = navigateToAfterLogin,
    )
  }
}
