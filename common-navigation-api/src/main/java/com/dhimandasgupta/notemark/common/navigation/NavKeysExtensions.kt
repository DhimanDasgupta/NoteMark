package com.dhimandasgupta.notemark.common.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun NavBackStack<NavKey>.clearPreLoginKeys(): Boolean = removeAll { navKey ->
  navKey is PreLoginNavKey
}

fun NavBackStack<NavKey>.clearPostLoginNavKeys(): Boolean = removeAll { navKey ->
  navKey is PostLoginNavKey
}

fun NavBackStack<NavKey>.clearNoteEditNavKeys(): Boolean = removeAll { navKey ->
  navKey is NoteEditNavKey
}

fun NavBackStack<NavKey>.isSettingsOpen(): Boolean = last() == SettingsNavKey

fun NavBackStack<NavKey>.removeSettingsKey(): Boolean = removeIf { navKey ->
  navKey == SettingsNavKey
}

/** Replaces the pre-login screens with the note list once the user is logged in. */
fun NavBackStack<NavKey>.goToNoteListAfterLogin() {
  clearPreLoginKeys()
  add(NoteListNavKey)
}

/**
 * Replaces the logged-in screens with the launcher after a logout. The note list and settings can
 * both handle the same logout, so only the first one to clear the logged-in screens adds the
 * launcher.
 */
fun NavBackStack<NavKey>.resetToLauncherAfterLogout() {
  if (clearPostLoginNavKeys()) {
    add(LauncherNavKey)
  }
}
