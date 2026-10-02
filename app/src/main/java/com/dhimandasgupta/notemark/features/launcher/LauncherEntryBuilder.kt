package com.dhimandasgupta.notemark.features.launcher

import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.app.di.LocalNoteMarkGraph
import com.dhimandasgupta.notemark.app.nav.LauncherNavKey

@Composable
fun EntryProviderScope<NavKey>.LauncherEntryBuilder(
  modifier: Modifier = Modifier,
  style: Style = Style,
  navigateAfterLogin: () -> Unit,
  navigateToLogin: () -> Unit,
) {
  entry<LauncherNavKey> {
    val graph = LocalNoteMarkGraph.current
    val launcherPresenter: LauncherPresenter = retain { graph.launcherPresenter() }

    LauncherEntry(
      modifier = modifier,
      style = style,
      launcherPresenter = launcherPresenter,
      navigateAfterLogin = navigateAfterLogin,
      navigateToLogin = navigateToLogin,
    )
  }
}
