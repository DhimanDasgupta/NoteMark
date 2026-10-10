package com.dhimandasgupta.notemark.features.launcher

import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.common.navigation.LauncherNavKey

@Composable
fun EntryProviderScope<NavKey>.LauncherEntryBuilder(
  modifier: Modifier = Modifier,
  style: Style = Style,
  launcherPresenter: () -> LauncherPresenter,
  navigateAfterLogin: () -> Unit,
  navigateToLogin: () -> Unit,
) {
  entry<LauncherNavKey> {
    val presenter: LauncherPresenter = retain { launcherPresenter() }

    LauncherEntry(
      modifier = modifier,
      style = style,
      launcherPresenter = presenter,
      navigateAfterLogin = navigateAfterLogin,
      navigateToLogin = navigateToLogin,
    )
  }
}
