package com.dhimandasgupta.notemark.ui.activity

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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.app.NoteMarkApp
import com.dhimandasgupta.notemark.app.di.LocalNoteMarkGraph
import com.dhimandasgupta.notemark.app.nav.DeepLinkParser
import com.dhimandasgupta.notemark.app.nav.LauncherNavKey
import com.dhimandasgupta.notemark.app.nav.NoteMarkRoot
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

class MainActivity : ComponentActivity() {
  private var initialNavKeys by mutableStateOf<PersistentList<NavKey>?>(null)

  @OptIn(ExperimentalFoundationApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    ComposeFoundationFlags.isInheritedTextStyleEnabled = true
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    initialNavKeys = DeepLinkParser.parse(intent?.data)

    setContent {
      val graph = (applicationContext as NoteMarkApp).getGraph()
      val initialKeys = initialNavKeys ?: persistentListOf(LauncherNavKey)
      NoteMarkTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          CompositionLocalProvider(LocalNoteMarkGraph provides graph) {
            NoteMarkRoot(
              modifier = Modifier.consumeWindowInsets(paddingValues = innerPadding),
              initialKeys = initialKeys,
            )
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    DeepLinkParser.parse(intent.data)?.let { keys ->
      initialNavKeys = keys
    }
  }
}
