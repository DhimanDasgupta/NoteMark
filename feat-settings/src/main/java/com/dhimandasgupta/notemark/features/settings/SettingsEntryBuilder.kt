package com.dhimandasgupta.notemark.features.settings

import androidx.compose.foundation.style.Style
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.common.navigation.SettingsNavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun EntryProviderScope<NavKey>.SettingsEntryBuilder(
  modifier: Modifier = Modifier,
  style: Style = Style,
  settingsPresenter: () -> SettingsPresenter,
  navigateToLauncherAfterLogout: () -> Unit,
  navigateUp: () -> Unit,
) {
  entry<SettingsNavKey>(metadata = ListDetailSceneStrategy.extraPane()) {
    val presenter: SettingsPresenter = retain { settingsPresenter() }

    SettingsEntry(
      modifier = modifier,
      style = style,
      settingsPresenter = presenter,
      navigateToLauncherAfterLogout = navigateToLauncherAfterLogout,
      navigateUp = navigateUp,
    )
  }
}
