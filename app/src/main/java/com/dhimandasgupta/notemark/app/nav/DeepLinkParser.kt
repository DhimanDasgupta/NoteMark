package com.dhimandasgupta.notemark.app.nav

import android.net.Uri
import androidx.navigation3.runtime.NavKey
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

object DeepLinkParser {
  fun parse(uri: Uri?): PersistentList<NavKey>? {
    if (uri == null) return null

    val isCustomScheme = uri.scheme == "notemark" && uri.host == "notes"
    val isHttpScheme = (uri.scheme == "https" || uri.scheme == "http")

    if (!isCustomScheme && !isHttpScheme) return null
    val isNewNotePath = uri.path == "/notes/new" || uri.path == "/new"

    return if (isNewNotePath) {
      persistentListOf(NoteListNavKey, NoteCreateNavKey)
    } else {
      null
    }
  }
}
