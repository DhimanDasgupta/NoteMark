package com.dhimandasgupta.notemark

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.dhimandasgupta.notemark.common.extensions.setDarkStatusBarIcons
import com.dhimandasgupta.notemark.presenter.AddNotePresenter
import com.dhimandasgupta.notemark.presenter.EditNotePresenter
import com.dhimandasgupta.notemark.presenter.LauncherPresenter
import com.dhimandasgupta.notemark.presenter.LoginPresenter
import com.dhimandasgupta.notemark.presenter.NoteListPresenter
import com.dhimandasgupta.notemark.presenter.RegistrationPresenter
import com.dhimandasgupta.notemark.presenter.SettingsPresenter
import com.dhimandasgupta.notemark.statemachine.AppAction
import com.dhimandasgupta.notemark.statemachine.NoteListAction.NoteClicked
import com.dhimandasgupta.notemark.ui.screens.AddNotePane
import com.dhimandasgupta.notemark.ui.screens.LauncherPane
import com.dhimandasgupta.notemark.ui.screens.LoginPane
import com.dhimandasgupta.notemark.ui.screens.EditNotePane
import com.dhimandasgupta.notemark.ui.screens.NoteListPane
import com.dhimandasgupta.notemark.ui.screens.RegistrationPane
import com.dhimandasgupta.notemark.ui.screens.SettingsPane
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Composable
fun NoteMarkRoot(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NoteMarkDestination.RootPane,
        modifier = modifier
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
                    noteListAction(NoteClicked(noteListUiModel.noteClickedUuid))
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