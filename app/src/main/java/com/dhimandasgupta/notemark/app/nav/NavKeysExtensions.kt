package com.dhimandasgupta.notemark.app.nav

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun NavBackStack<NavKey>.clearPreLoginKeys(): Boolean =
    removeAll { it is PreLoginNavKey }

fun NavBackStack<NavKey>.clearPostLoginNavKeys(): Boolean =
    removeAll { it is PostLoginNavKey }

fun NavBackStack<NavKey>.clearNoteEditNavKeys(): Boolean =
    removeAll { it is NoteEditNavKey }

fun NavBackStack<NavKey>.isSettingsOpen(): Boolean =
    last() == SettingsNavKey