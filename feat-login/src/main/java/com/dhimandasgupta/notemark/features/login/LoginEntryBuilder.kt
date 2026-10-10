package com.dhimandasgupta.notemark.features.login

import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.common.navigation.LoginNavKey

@Composable
fun EntryProviderScope<NavKey>.LoginEntryBuilder(
  modifier: Modifier = Modifier,
  style: Style = Style,
  loginPresenter: () -> LoginPresenter,
  navigateToRegistration: () -> Unit,
  navigateToAfterLogin: () -> Unit,
) {
  entry<LoginNavKey> {
    val presenter: LoginPresenter = retain { loginPresenter() }

    LoginEntry(
      modifier = modifier,
      style = style,
      loginPresenter = presenter,
      navigateToRegistration = navigateToRegistration,
      navigateToAfterLogin = navigateToAfterLogin,
    )
  }
}
