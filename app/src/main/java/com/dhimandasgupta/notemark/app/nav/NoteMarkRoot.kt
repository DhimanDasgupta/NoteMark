package com.dhimandasgupta.notemark.app.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.dhimandasgupta.notemark.features.addnote.NoteCreateEntryBuilder
import com.dhimandasgupta.notemark.features.editnote.NoteEditEntryBuilder
import com.dhimandasgupta.notemark.features.launcher.LauncherEntryBuilder
import com.dhimandasgupta.notemark.features.login.LoginEntryBuilder
import com.dhimandasgupta.notemark.features.notelist.NoteListEntryBuilder
import com.dhimandasgupta.notemark.features.registration.RegistrationEntryBuilder
import com.dhimandasgupta.notemark.features.settings.SettingsEntryBuilder
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NoteMarkRoot(
  modifier: Modifier,
  style: Style = Style,
  initialKeys: PersistentList<NavKey> = persistentListOf(LauncherNavKey),
) {
  val backStack = rememberNavBackStack(*initialKeys.toTypedArray())
  val sceneStrategy = rememberListDetailSceneStrategy<NavKey>()
  val decorators = listOf(rememberSaveableStateHolderNavEntryDecorator<NavKey>())

  LaunchedEffect(initialKeys) {
    if (initialKeys.isNotEmpty() && initialKeys != persistentListOf(LauncherNavKey)) {
      backStack.clear()
      backStack.addAll(initialKeys)
    }
  }

  SharedTransitionLayout(modifier = modifier.styleable(null, style)) {
    NavDisplay(
      modifier = Modifier.fillMaxSize(),
      backStack = backStack,
      entryDecorators = decorators,
      sceneStrategies = listOf(sceneStrategy),
      onBack = { backStack.removeLastOrNull() },
      sharedTransitionScope = this,
      transitionSpec = {
        slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start) {
          initialOffSet ->
          initialOffSet
        } togetherWith
          slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End) {
            initialOffSet ->
            -initialOffSet
          }
      },
      popTransitionSpec = {
        slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start) {
          initialOffSet ->
          -initialOffSet
        } togetherWith
          slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End) {
            initialOffSet ->
            initialOffSet
          }
      },
      predictivePopTransitionSpec = {
        slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start) {
          initialOffSet ->
          -initialOffSet
        } + fadeIn() togetherWith
          slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End) {
            initialOffSet ->
            initialOffSet
          } + fadeOut()
      },
      entryProvider =
        entryProvider {
          LauncherEntryBuilder(
            navigateAfterLogin = {
              backStack.apply {
                clearPreLoginKeys()
                add(NoteListNavKey)
              }
            },
            navigateToLogin = {
              backStack.add(LoginNavKey)
            },
          )
          LoginEntryBuilder(
            navigateToRegistration = {
              backStack.add(RegistrationNavKey)
            },
            navigateToAfterLogin = {
              backStack.apply {
                clearPreLoginKeys()
                add(NoteListNavKey)
              }
            },
          )
          RegistrationEntryBuilder(
            navigateToLoginFromRegistration = {
              backStack.removeLastOrNull()
            }
          )
          NoteListEntryBuilder(
            navigateToLauncherIfLoggedOut = {
              backStack.apply {
                clearPostLoginNavKeys()
                add(LauncherNavKey)
              }
            },
            navigateToAdd = {
              backStack.add(NoteCreateNavKey)
            },
            navigateToEdit = { uuid ->
              backStack.apply {
                clearNoteEditNavKeys()
                add(NoteEditNavKey(uuid))
              }
            },
            navigateToSettings = {
              if (!backStack.isSettingsOpen()) {
                backStack.add(SettingsNavKey)
              }
            },
          )
          NoteCreateEntryBuilder(
            navigateUp = {
              backStack.removeLastOrNull()
            }
          )
          NoteEditEntryBuilder(
            navigateUp = {
              backStack.clearNoteEditNavKeys()
            }
          )
          SettingsEntryBuilder(
            navigateToLauncherAfterLogout = {
              backStack.apply {
                clearPostLoginNavKeys()
                add(LauncherNavKey)
              }
            },
            navigateUp = {
              backStack.removeSettingsKey()
            },
          )
        },
    )
  }
}
