package com.dhimandasgupta.notemark.common.navigation

import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Registers one feature's Navigation 3 entries. Each feature contributes an injected implementation
 * into the app graph's `Set<EntryInstaller>`, so the dependencies it needs (its presenter provider)
 * arrive through its constructor rather than from the root.
 *
 * The back stack is passed in at install time because it is owned by the composition, not the
 * graph. Implementations are stable because they are graph-created and hold only providers.
 */
@Stable
fun interface EntryInstaller {
  fun EntryProviderScope<NavKey>.install(backStack: NavBackStack<NavKey>)
}
