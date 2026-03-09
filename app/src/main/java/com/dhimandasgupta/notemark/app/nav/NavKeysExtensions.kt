package com.dhimandasgupta.notemark.app.nav

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun NavBackStack<NavKey>.clearPreLoginKeys(): Boolean =
    removeAll { navKey -> navKey is PreLoginNavKey }

fun NavBackStack<NavKey>.clearPostLoginNavKeys(): Boolean =
    removeAll { navKey -> navKey is PostLoginNavKey }

fun NavBackStack<NavKey>.clearNoteEditNavKeys(): Boolean =
    removeAll { navKey -> navKey is NoteEditNavKey }

fun NavBackStack<NavKey>.isSettingsOpen(): Boolean =
    last() == SettingsNavKey

fun NavBackStack<NavKey>.removeSettingsKey(): Boolean =
    removeIf { navKey -> navKey == SettingsNavKey }