package com.dhimandasgupta.notemark.app.nav

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.common.navigation.EntryInstaller
import com.dhimandasgupta.notemark.common.navigation.LauncherNavKey
import com.dhimandasgupta.notemark.common.navigation.NoteCreateNavKey
import com.dhimandasgupta.notemark.common.navigation.NoteListNavKey
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class NoteMarkRootDeepLinkTest {
  @get:Rule val composeTestRule = createComposeRule()

  private val newNote = persistentListOf<NavKey>(NoteListNavKey, NoteCreateNavKey)
  private var pendingDeepLinkKeys by mutableStateOf<PersistentList<NavKey>?>(null)
  private lateinit var backStack: NavBackStack<NavKey>

  @Test
  fun sameDeepLinkWorksAgainAfterGoingBack() {
    composeTestRule.setContent {
      NoteMarkRoot(
        entryInstallers = persistentSetOf(testEntries),
        modifier = Modifier,
        pendingDeepLinkKeys = pendingDeepLinkKeys,
        onDeepLinkConsumed = { pendingDeepLinkKeys = null },
      )
    }
    composeTestRule.onNodeWithText("Launcher").assertIsDisplayed()

    repeat(times = 2) {
      composeTestRule.runOnIdle { pendingDeepLinkKeys = newNote }
      composeTestRule.runOnIdle {
        assertEquals(newNote, backStack.toList())
        assertNull(pendingDeepLinkKeys)
        backStack.removeLastOrNull()
      }
      composeTestRule.onNodeWithText("Note list").assertIsDisplayed()
    }
  }

  private val testEntries = EntryInstaller { installedBackStack ->
    backStack = installedBackStack
    entry<LauncherNavKey> { Text(text = "Launcher") }
    entry<NoteListNavKey> { Text(text = "Note list") }
    entry<NoteCreateNavKey> { Text(text = "Note create") }
  }
}
