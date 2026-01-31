package com.dhimandasgupta.notemark.app.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.dhimandasgupta.notemark.features.addnote.AddNoteEntry
import com.dhimandasgupta.notemark.features.editnote.EditNoteEntry
import com.dhimandasgupta.notemark.features.launcher.LauncherEntry
import com.dhimandasgupta.notemark.features.login.LoginEntry
import com.dhimandasgupta.notemark.features.notelist.NoteListEntry
import com.dhimandasgupta.notemark.features.registration.RegistrationEntry
import com.dhimandasgupta.notemark.features.settings.SettingsEntry

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
            entry<LauncherNavKey> {
                LauncherEntry(
                    modifier = modifier,
                    navigateAfterLogin = {
                        backStack.clearPreLoginKeys()
                        backStack.add(NoteListNavKey)
                    },
                    navigateToLogin = {
                        backStack.add(LoginNavKey)
                    }
                )
            }
            entry<LoginNavKey> {
                LoginEntry(
                    modifier = modifier,
                    navigateToRegistration = {
                        backStack.add(RegistrationNavKey)
                    },
                    navigateToAfterLogin = {
                        backStack.clearPreLoginKeys()
                        backStack.add(NoteListNavKey)
                    }
                )
            }
            entry<RegistrationNavKey> {
                RegistrationEntry(
                    modifier = modifier,
                    navigateToLoginFromRegistration = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<NoteListNavKey>(
                metadata = ListDetailSceneStrategy.listPane()
            ) {
                NoteListEntry(
                    modifier = modifier,
                    navigateToAdd = {
                        backStack.add(NoteCreateNavKey)
                    },
                    navigateToEdit = { uuid ->
                        backStack.add(NoteEditNavKey(uuid))
                    },
                    navigateToSettings = {
                        backStack.add(SettingsNavKey)
                    }
                )
            }
            entry<NoteCreateNavKey>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                AddNoteEntry(
                    modifier = modifier,
                    navigateUp = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<NoteEditNavKey>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                EditNoteEntry(
                    modifier = modifier,
                    argument = it.noteId,
                    navigateUp = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<SettingsNavKey>(
                metadata = ListDetailSceneStrategy.extraPane()
            ) {
                SettingsEntry(
                    modifier = modifier,
                    navigateToLauncherAfterLogout = {
                        backStack.clearPostLoginNavKeys()
                        backStack.add(LauncherNavKey)
                    },
                    navigateUp = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}