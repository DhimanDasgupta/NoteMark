package com.dhimandasgupta.notemark.app.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.dhimandasgupta.notemark.features.addnote.NoteCreateEntryBuilder
import com.dhimandasgupta.notemark.features.editnote.NoteEditEntryBuilder
import com.dhimandasgupta.notemark.features.launcher.LauncherEntryBuilder
import com.dhimandasgupta.notemark.features.login.LoginEntryBuilder
import com.dhimandasgupta.notemark.features.notelist.NoteListEntryBuilder
import com.dhimandasgupta.notemark.features.registration.RegistrationEntryBuilder
import com.dhimandasgupta.notemark.features.settings.SettingsEntryBuilder

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NoteMarkRoot(
    modifier: Modifier
) {
    val backStack = rememberNavBackStack(LauncherNavKey)
    val sceneStrategy = rememberListDetailSceneStrategy<NavKey>()

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        sceneStrategy = sceneStrategy,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
            ) { initialOffSet -> initialOffSet } togetherWith slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
            ) { initialOffSet -> -initialOffSet }
        },
        popTransitionSpec = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
            ) { initialOffSet -> -initialOffSet } togetherWith slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
            ) { initialOffSet -> initialOffSet }
        },
        predictivePopTransitionSpec = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
            ) { initialOffSet -> -initialOffSet } + fadeIn() togetherWith slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
            ) { initialOffSet -> initialOffSet } + fadeOut()
        },
        entryProvider = entryProvider {
            LauncherEntryBuilder(
                modifier = modifier,
                navigateAfterLogin = {
                    backStack.apply {
                        clearPreLoginKeys()
                        add(NoteListNavKey)
                    }
                },
                navigateToLogin = {
                    backStack.add(LoginNavKey)
                }
            )
            LoginEntryBuilder(
                modifier = modifier,
                navigateToRegistration = {
                    backStack.add(RegistrationNavKey)
                },
                navigateToAfterLogin = {
                    backStack.apply {
                        clearPreLoginKeys()
                        add(NoteListNavKey)
                    }
                }
            )
            RegistrationEntryBuilder(
                modifier = modifier,
                navigateToLoginFromRegistration = {
                    backStack.removeLastOrNull()
                }
            )
            NoteListEntryBuilder(
                modifier = modifier,
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
                }
            )
            NoteCreateEntryBuilder(
                modifier = modifier,
                navigateUp = {
                    backStack.removeLastOrNull()
                }
            )
            NoteEditEntryBuilder(
                modifier = modifier,
                navigateUp = {
                    backStack.removeLastOrNull()
                }
            )
            SettingsEntryBuilder(
                modifier = modifier,
                navigateToLauncherAfterLogout = {
                    backStack.apply {
                        clearPostLoginNavKeys()
                        add(LauncherNavKey)
                    }
                },
                navigateUp = {
                    backStack.removeLastOrNull()
                }
            )
        }
    )
}