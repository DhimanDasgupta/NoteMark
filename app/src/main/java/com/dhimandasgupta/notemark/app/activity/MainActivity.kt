package com.dhimandasgupta.notemark.app.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.app.NoteMarkApp
import com.dhimandasgupta.notemark.app.nav.DeepLinkParser
import com.dhimandasgupta.notemark.app.nav.NoteMarkRoot
import com.dhimandasgupta.notemark.common.navigation.LauncherNavKey
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  /** Null while the deep link that started the activity is still being resolved. */
  private var initialKeys by mutableStateOf<PersistentList<NavKey>?>(null)

  /** A deep link received by the running activity, until it has replaced the back stack. */
  private var pendingDeepLinkKeys by mutableStateOf<PersistentList<NavKey>?>(null)

  private val deepLinkLoginGuard by lazy {
    (application as NoteMarkApp).getGraph().deepLinkLoginGuard
  }

  @OptIn(ExperimentalFoundationApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    val splashScreen = installSplashScreen()
    ComposeFoundationFlags.isInheritedTextStyleEnabled = true
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // A recreated activity restores its back stack. Its intent has been handled already.
    val deepLinkKeys = if (savedInstanceState == null) DeepLinkParser.parse(intent) else null
    if (deepLinkKeys == null) {
      initialKeys = persistentListOf(LauncherNavKey)
    } else {
      lifecycleScope.launch { initialKeys = deepLinkLoginGuard.guard(deepLinkKeys) }
    }
    splashScreen.setKeepOnScreenCondition { initialKeys == null }

    val entryInstallers = (application as NoteMarkApp).getGraph().entryInstallers.toPersistentSet()

    setContent {
      NoteMarkTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          initialKeys?.let { keys ->
            NoteMarkRoot(
              entryInstallers = entryInstallers,
              modifier = Modifier.consumeWindowInsets(paddingValues = innerPadding),
              initialKeys = keys,
              pendingDeepLinkKeys = pendingDeepLinkKeys,
              onDeepLinkConsumed = { pendingDeepLinkKeys = null },
            )
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    DeepLinkParser.parse(intent)?.let { keys ->
      lifecycleScope.launch { pendingDeepLinkKeys = deepLinkLoginGuard.guard(keys) }
    }
  }
}
