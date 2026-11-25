package com.dhimandasgupta.notemark.app.nav

import android.content.Intent
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.BackStackMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.invoke
import androidx.navigation3.runtime.deeplink.withBackStack
import com.dhimandasgupta.notemark.common.navigation.NoteCreateNavKey
import com.dhimandasgupta.notemark.common.navigation.NoteListNavKey
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

object DeepLinkParser {
  /** Every supported deep link, with the back stack to show when it matches. */
  private val matchers: List<BackStackMatcher<NavKey, NavKey>> =
    listOf(
      UriDeepLinkMatcher(
          uriPattern = "notemark://notes/new".toUri(),
          serializer = NoteCreateNavKey.serializer(),
        )
        .withBackStack { match -> listOf(NoteListNavKey, match.key) }
    )

  /** Returns the back stack for the deep link in [intent], or null if it has no supported one. */
  fun parse(intent: Intent?): PersistentList<NavKey>? {
    // DeepLinkRequest throws for an intent without data, action, type and extras.
    if (intent?.data == null) return null

    val request = DeepLinkRequest(intent)
    return matchers
      .mapNotNull { matcher -> matcher.match(request) }
      .maxOrNull()
      ?.backStack
      ?.toPersistentList()
  }
}
