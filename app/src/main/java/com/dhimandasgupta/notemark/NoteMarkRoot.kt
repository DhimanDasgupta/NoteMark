package com.dhimandasgupta.notemark

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.dhimandasgupta.notemark.NoteMarkDestination.NoteEditPane
import com.dhimandasgupta.notemark.presenter.AppPresenter
import com.dhimandasgupta.notemark.presenter.EditNotePresenter
import com.dhimandasgupta.notemark.presenter.LoginPresenter
import com.dhimandasgupta.notemark.presenter.NoteListPresenter
import com.dhimandasgupta.notemark.presenter.RegistrationPresenter
import com.dhimandasgupta.notemark.statemachine.AppAction
import com.dhimandasgupta.notemark.statemachine.AppState
import com.dhimandasgupta.notemark.statemachine.NonLoggedInState
import com.dhimandasgupta.notemark.statemachine.NoteListAction
import com.dhimandasgupta.notemark.statemachine.NoteListAction.NoteClicked
import com.dhimandasgupta.notemark.ui.screens.LauncherPane
import com.dhimandasgupta.notemark.ui.screens.NoteListPane
import com.dhimandasgupta.notemark.ui.screens.LoginPane
import com.dhimandasgupta.notemark.ui.screens.NoteEditPane
import com.dhimandasgupta.notemark.ui.screens.RegistrationPane
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Composable
fun NoteMarkRoot(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val appPresenter = koinInject<AppPresenter>()

    val  appUiModel = appPresenter.uiModel()
    val  appEvents = appPresenter::processEvent

    NavHost(
        navController = navController,
        startDestination = NoteMarkDestination.RootDestination,
        modifier = modifier
    ) {
        NoteMarkGraph(
            appUiModel = appUiModel,
            appEvents = appEvents,
            navController = navController,
            windowSizeClass = windowSizeClass
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
private fun NavGraphBuilder.NoteMarkGraph(
    appUiModel: AppState,
    appEvents: (AppAction) -> Unit = {},
    navController: NavHostController,
    windowSizeClass: WindowSizeClass
) {
    navigation<NoteMarkDestination.RootDestination>(
        startDestination = NoteMarkDestination.LauncherDestination
    ) {
        composable<NoteMarkDestination.LauncherDestination> {
            val context  = LocalActivity.current

            LauncherPane(
                windowSizeClass = windowSizeClass,
                navigateToAfterLogin = {
                    if (appUiModel is NonLoggedInState) {
                        Toast.makeText(context, "Oops!!! Please login first to get started", Toast.LENGTH_LONG).show()
                        return@LauncherPane
                    }
                    navController.navigate(NoteMarkDestination.NoteListPane) {
                        popUpTo(NoteMarkDestination.LauncherDestination) {
                            inclusive = true
                        }
                    }
                },
                navigateToLogin = {
                    navController.navigate(NoteMarkDestination.LoginDestination) {
                        popUpTo(NoteMarkDestination.LauncherDestination) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<NoteMarkDestination.LoginDestination> {
            val loginPresenter = koinInject<LoginPresenter>()

            val loginUiModel = loginPresenter.uiModel()
            val loginEvents = loginPresenter::processEvent

            LoginPane(
                windowSizeClass = windowSizeClass,
                navigateToAfterLogin = {
                    navController.navigate(NoteMarkDestination.NoteListPane) {
                        popUpTo(NoteMarkDestination.LoginDestination) {
                            inclusive = true
                        }
                    }
                },
                navigateToRegistration = {
                    navController.navigate(NoteMarkDestination.RegistrationDestination) {
                        popUpTo(NoteMarkDestination.LoginDestination) {
                            inclusive = true
                        }
                    }
                },
                loginState = loginUiModel,
                loginAction = loginEvents,
                modifier = Modifier
            )
        }

        composable<NoteMarkDestination.RegistrationDestination> {
            val registrationPresenter = koinInject<RegistrationPresenter>()

            val registrationUiModel = registrationPresenter.uiModel()
            val registrationAction = registrationPresenter::processEvent

            RegistrationPane(
                modifier = Modifier,
                windowSizeClass = windowSizeClass,
                navigateToLogin = {
                    navController.navigate(NoteMarkDestination.LoginDestination) {
                        popUpTo(NoteMarkDestination.RegistrationDestination) {
                            inclusive = true
                        }
                    }
                },
                registrationState = registrationUiModel,
                registrationAction = registrationAction,
            )
        }

        composable<NoteMarkDestination.NoteListPane> {
            val noteListPresenter = koinInject<NoteListPresenter>()

            val noteListUiModel = noteListPresenter.uiModel()
            val noteListAction = noteListPresenter::processEvent


            NoteListPane(
                modifier = Modifier,
                windowSizeClass = windowSizeClass,
                appState = appUiModel,
                noteListUiModel = noteListUiModel,
                noteListAction = noteListAction,
                onNoteClicked = { uuid ->
                    navController.navigate(route = NoteEditPane(uuid))
                    noteListAction(NoteClicked(noteListUiModel.noteClickedUuid))
                },
                onFabClicked = {
                    navController.navigate(NoteEditPane(""))
                },
                onLogoutClicked = {
                    appEvents(AppAction.AppLogout)
                    navController.navigate(NoteMarkDestination.LauncherDestination) {
                        popUpTo(NoteMarkDestination.NoteListPane) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<NoteEditPane> { backStackEntry ->
            val arguments: NoteMarkDestination.NoteEditPane = backStackEntry.toRoute()

            val editNotePresenter = koinInject<EditNotePresenter>()

            val editNoteUiModel = editNotePresenter.uiModel()
            val editNoteAction = editNotePresenter::processEvent

            NoteEditPane(
                modifier = Modifier,
                windowSizeClass = windowSizeClass,
                noteId = arguments.noteId,
                editNoteUiModel = editNoteUiModel,
                editNoteAction = editNoteAction,
                onCloseClicked = { navController.navigateUp() }
            )
        }
    }
}

object NoteMarkDestination {
    @Serializable
    data object RootDestination

    @Serializable
    data object LauncherDestination

    @Serializable
    data object LoginDestination

    @Serializable
    data object RegistrationDestination

    @Serializable
    data object NoteListPane

    @Serializable
    data class NoteEditPane(
        val noteId: String = ""
    )
}