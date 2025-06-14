package com.dhimandasgupta.notemark

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.dhimandasgupta.notemark.presenter.AppPresenter
import com.dhimandasgupta.notemark.presenter.LoginPresenter
import com.dhimandasgupta.notemark.presenter.RegistrationPresenter
import com.dhimandasgupta.notemark.statemachine.AppAction
import com.dhimandasgupta.notemark.statemachine.AppState
import com.dhimandasgupta.notemark.ui.screens.LauncherPane
import com.dhimandasgupta.notemark.ui.screens.LoggedInPane
import com.dhimandasgupta.notemark.ui.screens.LoginPane
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
    appEvents: (AppAction) -> Unit,
    navController: NavHostController,
    windowSizeClass: WindowSizeClass
) {
    navigation<NoteMarkDestination.RootDestination>(
        startDestination = NoteMarkDestination.LauncherDestination
    ) {
        composable<NoteMarkDestination.LauncherDestination> {
            LauncherPane(
                windowSizeClass = windowSizeClass,
                navigateToAfterLogin = {
                    navController.navigate(NoteMarkDestination.LoggedInDestination) {
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
                    navController.navigate(NoteMarkDestination.LoggedInDestination) {
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

        composable<NoteMarkDestination.LoggedInDestination> {
            LoggedInPane(
                modifier = Modifier,
                windowSizeClass = windowSizeClass,
                appState = appUiModel,
                logoutClicked = {
                    appEvents(AppAction.AppLogout)
                    navController.navigate(NoteMarkDestination.LauncherDestination) {
                        popUpTo(NoteMarkDestination.LoggedInDestination) {
                            inclusive = true
                        }
                    }
                }
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
    data object LoggedInDestination
}