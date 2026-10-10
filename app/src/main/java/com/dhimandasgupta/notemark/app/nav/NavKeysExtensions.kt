package com.dhimandasgupta.notemark.app.nav

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.common.navigation.NoteEditNavKey

fun NavBackStack<NavKey>.clearNoteEditNavKeys(): Boolean = removeAll { navKey ->
  navKey is NoteEditNavKey
}

fun NavBackStack<NavKey>.isSettingsOpen(): Boolean = last() == SettingsNavKey

fun NavBackStack<NavKey>.removeSettingsKey(): Boolean = removeIf { navKey ->
  navKey == SettingsNavKey
}
