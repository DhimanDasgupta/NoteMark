package com.dhimandasgupta.notemark.app

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.dhimandasgupta.notemark.common.extensions.setDarkStatusBarIcons
import com.dhimandasgupta.notemark.features.addnote.AddNotePresenter
import com.dhimandasgupta.notemark.features.editnote.EditNotePresenter
import com.dhimandasgupta.notemark.features.launcher.AppAction
import com.dhimandasgupta.notemark.features.launcher.LauncherPresenter
import com.dhimandasgupta.notemark.features.login.LoginPresenter
import com.dhimandasgupta.notemark.features.notelist.NoteListPresenter
import com.dhimandasgupta.notemark.features.registration.RegistrationPresenter
import com.dhimandasgupta.notemark.features.settings.SettingsPresenter
import com.dhimandasgupta.notemark.features.addnote.AddNotePane
import com.dhimandasgupta.notemark.features.launcher.LauncherPane
import com.dhimandasgupta.notemark.features.login.LoginPane
import com.dhimandasgupta.notemark.features.editnote.EditNotePane
import com.dhimandasgupta.notemark.features.notelist.NoteListPane
import com.dhimandasgupta.notemark.features.registration.RegistrationPane
import com.dhimandasgupta.notemark.features.settings.SettingsPane
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Composable
fun NoteMarkRoot(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NoteMarkDestination.RootPane,
        enterTransition = { slideIn { IntOffset(x = it.width, y = 0) } },
        exitTransition = { slideOut { IntOffset(x = -it.width / 3, y = 0) } },
        popEnterTransition = { slideIn { IntOffset(x = -it.width, y = 0) } },
        popExitTransition = { slideOut { IntOffset(x = it.width, y = 0) } }
    ) {
        noteMarkGraph(
            navController = navController,
            windowSizeClass = windowSizeClass
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
private fun NavGraphBuilder.noteMarkGraph(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass
) {
    navigation<NoteMarkDestination.RootPane>(
        startDestination = NoteMarkDestination.LauncherPane
    ) {
        composable<NoteMarkDestination.LauncherPane> {
            val context = LocalActivity.current
            SideEffect { context?.setDarkStatusBarIcons(true) }

            val launcherPresenter = koinInject<LauncherPresenter>()
            val launcherUiModel = launcherPresenter.uiModel()

            BackHandler(
                enabled = launcherUiModel.loggedInUser == null
            ) {
                context?.finish()
            }

            LauncherPane(
                windowSizeClass = windowSizeClass,
                launcherUiModel = launcherUiModel,
                navigateToAfterLogin = {
                    if (launcherUiModel.loggedInUser == null) {
                        Toast.makeText(context, "Oops!!! Please login first to get started", Toast.LENGTH_LONG).show()
                        return@LauncherPane
                    }
                    navController.navigate(NoteMarkDestination.NoteListPane) {
                        popUpTo(NoteMarkDestination.LauncherPane) {
                            inclusive = true
                        }
                    }
                },
                navigateToLogin = {
                    navController.navigate(NoteMarkDestination.LoginPane) {
                        popUpTo(NoteMarkDestination.LauncherPane) {
                            inclusive = true
                        }
                    }
                },
                navigateToList = {
                    navController.navigate(NoteMarkDestination.NoteListPane) {
                        popUpTo(NoteMarkDestination.LauncherPane) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<NoteMarkDestination.LoginPane> {
            val context = LocalActivity.current
            SideEffect { context?.setDarkStatusBarIcons(false) }

            val loginPresenter = koinInject<LoginPresenter>()

            val loginUiModel = loginPresenter.uiModel()
            val loginEvents = loginPresenter::processEvent

            LoginPane(
                windowSizeClass = windowSizeClass,
                navigateToAfterLogin = {
                    navController.navigate(NoteMarkDestination.NoteListPane) {
                        popUpTo(NoteMarkDestination.LoginPane) {
                            inclusive = true
                        }
                    }
                },
                navigateToRegistration = {
                    navController.navigate(NoteMarkDestination.RegistrationPane) {
                        popUpTo(NoteMarkDestination.LoginPane) {
                            inclusive = true
                        }
                    }
                },
                loginUiModel = loginUiModel,
                loginAction = loginEvents,
                modifier = Modifier
            )
        }

        composable<NoteMarkDestination.RegistrationPane> {
            val context = LocalActivity.current
            SideEffect { context?.setDarkStatusBarIcons(false) }

            val registrationPresenter = koinInject<RegistrationPresenter>()
            val registrationUiModel = registrationPresenter.uiModel()
            val registrationAction = registrationPresenter::processEvent

            RegistrationPane(
                modifier = Modifier,
                windowSizeClass = windowSizeClass,
                navigateToLogin = {
                    navController.navigate(NoteMarkDestination.LoginPane) {
                        popUpTo(NoteMarkDestination.RegistrationPane) {
                            inclusive = true
                        }
                    }
                },
                registrationUiModel = registrationUiModel,
                registrationAction = registrationAction,
            )
        }

        composable<NoteMarkDestination.NoteListPane> {
            val context = LocalActivity.current
            SideEffect { context?.setDarkStatusBarIcons(true) }

            val noteListPresenter = koinInject<NoteListPresenter>()
            val noteListUiModel = noteListPresenter.uiModel()
            val noteListAction = noteListPresenter::processEvent

            NoteListPane(
                modifier = Modifier,
                windowSizeClass = windowSizeClass,
                noteListUiModel = noteListUiModel,
                noteListAction = noteListAction,
                onNoteClicked = { uuid ->
                    navController.navigate(route = NoteMarkDestination.NoteEditPane(uuid))
                },
                onFabClicked = {
                    navController.navigate(NoteMarkDestination.NoteCreatePane)
                },
                onSettingsClicked = {
                    navController.navigate(NoteMarkDestination.SettingsPane)
                },
                onProfileClicked = {}
            )
        }

        composable<NoteMarkDestination.NoteCreatePane> {
            val context = LocalActivity.current
            SideEffect { context?.setDarkStatusBarIcons(true) }

            val addNotePresenter = koinInject<AddNotePresenter>()
            val addNoteUiModel = addNotePresenter.uiModel()
            val addNoteAction = addNotePresenter::processEvent

            AddNotePane(
                modifier = Modifier,
                windowSizeClass = windowSizeClass,
                addNoteUiModel = addNoteUiModel,
                addNoteAction = addNoteAction,
                onBackClicked = { navController.navigateUp() }
            )
        }

        composable<NoteMarkDestination.NoteEditPane> { backStackEntry ->
            val context = LocalActivity.current
            SideEffect { context?.setDarkStatusBarIcons(true) }

            val arguments: NoteMarkDestination.NoteEditPane = backStackEntry.toRoute()
            val editNotePresenter = koinInject<EditNotePresenter>()
            val editNoteUiModel = editNotePresenter.uiModel()
            val editNoteAction = editNotePresenter::processEvent

            EditNotePane(
                modifier = Modifier,
                windowSizeClass = windowSizeClass,
                noteId = arguments.noteId,
                editNoteUiModel = editNoteUiModel,
                editNoteAction = editNoteAction,
                onCloseClicked = { navController.navigateUp() }
            )
        }

        composable<NoteMarkDestination.SettingsPane> {
            val context = LocalActivity.current
            SideEffect { context?.setDarkStatusBarIcons(true) }

            val settingsPresenter = koinInject<SettingsPresenter>()
            val settingsUiModel = settingsPresenter.uiModel()
            val settingsAction = settingsPresenter::processEvent

            SettingsPane(
                modifier = Modifier,
                settingsUiModel = settingsUiModel,
                settingsAction = settingsAction,
                onBackClicked = { navController.navigateUp() },
                onLogoutSuccessful = {
                    navController.navigate(NoteMarkDestination.LauncherPane) {
                        launchSingleTop = true
                    }
                },
                onLogoutClicked = {
                    settingsAction(AppAction.AppLogout)
                }
            )
        }
    }
}

object NoteMarkDestination {
    @Serializable
    data object RootPane

    @Serializable
    data object LauncherPane

    @Serializable
    data object LoginPane

    @Serializable
    data object RegistrationPane

    @Serializable
    data object NoteListPane

    @Serializable
    data object NoteCreatePane

    @Serializable
    data class NoteEditPane(
        val noteId: String = ""
    )

    @Serializable
    data object SettingsPane
}