package com.dhimandasgupta.notemark.app.extension

import android.content.Context
import com.dhimandasgupta.notemark.common.android.AppShortcuts
import dev.zacsweers.metro.Inject

@Inject
class ShortcutManagerAppShortcuts(private val context: Context) : AppShortcuts {
  override fun addCreateNewNoteShortcut() = context.addCreateNewNoteShortcut()

  override fun removeCreateNewNoteShortcut() = context.removeCreateNewNoteShortcut()
}
