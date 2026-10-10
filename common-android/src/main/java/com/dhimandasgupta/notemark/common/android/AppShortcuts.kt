package com.dhimandasgupta.notemark.common.android

/** Launcher shortcuts that depend on login state. The app module provides the implementation. */
interface AppShortcuts {
  fun addCreateNewNoteShortcut()

  fun removeCreateNewNoteShortcut()
}
