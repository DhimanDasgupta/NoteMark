package com.dhimandasgupta.notemark.app.nav

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.dhimandasgupta.notemark.common.extensions.setDarkStatusBarIcons
import com.dhimandasgupta.notemark.features.addnote.AddNotePane
import com.dhimandasgupta.notemark.features.addnote.AddNotePresenter
import com.dhimandasgupta.notemark.features.addnote.AddNoteUiModel
import com.dhimandasgupta.notemark.features.editnote.EditNotePane
import com.dhimandasgupta.notemark.features.editnote.EditNotePresenter
import com.dhimandasgupta.notemark.features.editnote.EditNoteUiModel
import com.dhimandasgupta.notemark.features.launcher.AppAction
import com.dhimandasgupta.notemark.features.launcher.LauncherPane
import com.dhimandasgupta.notemark.features.launcher.LauncherPresenter
import com.dhimandasgupta.notemark.features.launcher.LauncherUiModel
import com.dhimandasgupta.notemark.features.login.LoginPane
import com.dhimandasgupta.notemark.features.login.LoginPresenter
import com.dhimandasgupta.notemark.features.login.LoginUiModel
import com.dhimandasgupta.notemark.features.notelist.NoteListPane
import com.dhimandasgupta.notemark.features.notelist.NoteListPresenter
import com.dhimandasgupta.notemark.features.notelist.NoteListUiModel
import com.dhimandasgupta.notemark.features.registration.RegistrationPane
import com.dhimandasgupta.notemark.features.registration.RegistrationPresenter
import com.dhimandasgupta.notemark.features.registration.RegistrationUiModel
import com.dhimandasgupta.notemark.features.settings.SettingsPane
import com.dhimandasgupta.notemark.features.settings.SettingsPresenter
import com.dhimandasgupta.notemark.features.settings.SettingsUiModel
import kotlinx.coroutines.cancel
import kotlinx.serialization.Serializable
import org.koin.java.KoinJavaComponent.get

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

            val launcherPresenter: LauncherPresenter = remember { get(clazz = LauncherPresenter::class.java) }
            var launcherUiModel by remember { mutableStateOf(LauncherUiModel.Empty) }

            val scope = rememberCoroutineScope()
            LifecycleStartEffect(key1 = Unit) {
                scope.launchMolecule(mode = RecompositionMode.Immediate) {
                    launcherUiModel = launcherPresenter.uiModel()
                }
                onStopOrDispose {
                    scope.cancel()
                }
            }

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
                    navController.navigate(route = NoteMarkDestination.NoteListPane) {
                        popUpTo(route = NoteMarkDestination.LauncherPane) {
                            inclusive = true
                        }
                    }
                },
                navigateToLogin = {
                    navController.navigate(route = NoteMarkDestination.LoginPane) {
                        popUpTo(route = NoteMarkDestination.LauncherPane) {
                            inclusive = true
                        }
                    }
                },
                navigateToList = {
                    navController.navigate(route = NoteMarkDestination.NoteListPane) {
                        popUpTo(route = NoteMarkDestination.LauncherPane) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<NoteMarkDestination.LoginPane> {
            val context = LocalActivity.current
            SideEffect { context?.setDarkStatusBarIcons(false) }

            val loginPresenter: LoginPresenter = remember { get(clazz = LoginPresenter::class.java) }

            var loginUiModel by remember { mutableStateOf(LoginUiModel.Empty) }
            val loginEvents = loginPresenter::processEvent

            val scope = rememberCoroutineScope()
            LifecycleStartEffect(key1 = Unit) {
                scope.launchMolecule(mode = RecompositionMode.Immediate) {
                    loginUiModel = loginPresenter.uiModel()
                }
                onStopOrDispose {
                    scope.cancel()
                }
            }

            /**
             * To make sure the collection from AppState machine is canceled
             * Otherwise FlowRedux will throw an exception
             * */
            BackHandler(
                enabled = true,
            ) {
                navController.navigateUp()
            }

            LoginPane(
                windowSizeClass = windowSizeClass,
                navigateToAfterLogin = {
                    navController.navigate(route = NoteMarkDestination.NoteListPane) {
                        popUpTo(route = NoteMarkDestination.LoginPane) {
                            inclusive = true
                        }
                    }
                },
                navigateToRegistration = {
                    navController.navigate(route = NoteMarkDestination.RegistrationPane) {
                        popUpTo(route = NoteMarkDestination.LoginPane) {
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

            val registrationPresenter: RegistrationPresenter = remember { get(clazz = RegistrationPresenter::class.java) }
            var registrationUiModel by remember { mutableStateOf(RegistrationUiModel.Empty) }
            val registrationAction = registrationPresenter::processEvent

            val scope = rememberCoroutineScope()
            LifecycleStartEffect(key1 = Unit) {
                scope.launchMolecule(mode = RecompositionMode.Immediate) {
                    registrationUiModel = registrationPresenter.uiModel()
                }
                onStopOrDispose {
                    scope.cancel()
                }
            }

            /**
             * To make sure the collection from AppState machine is canceled
             * Otherwise FlowRedux will throw an exception
             * */
            BackHandler(
                enabled = true,
            ) {
                navController.navigateUp()
            }

            RegistrationPane(
                modifier = Modifier,
                windowSizeClass = windowSizeClass,
                navigateToLogin = {
                    navController.navigate(route = NoteMarkDestination.LoginPane) {
                        popUpTo(route = NoteMarkDestination.RegistrationPane) {
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

            val noteListPresenter: NoteListPresenter = remember { get(clazz = NoteListPresenter::class.java) }
            var noteListUiModel by remember { mutableStateOf(NoteListUiModel.Empty) }
            val noteListAction = noteListPresenter::processEvent

            val scope = rememberCoroutineScope()
            LifecycleStartEffect(key1 = Unit) {
                scope.launchMolecule(mode = RecompositionMode.Immediate) {
                    noteListUiModel = noteListPresenter.uiModel()
                }
                onStopOrDispose {
                    scope.cancel()
                }
            }

            val activity = LocalActivity.current

            /**
             * To make sure the collection from AppState machine is canceled
             * Otherwise FlowRedux will throw an exception
             * */
            BackHandler(
                enabled = true,
            ) {
                activity?.finish()
            }

            NoteListPane(
                modifier = Modifier,
                windowSizeClass = windowSizeClass,
                noteListUiModel = noteListUiModel,
                noteListAction = noteListAction,
                onNoteClicked = { uuid ->
                    navController.navigate(route = NoteMarkDestination.NoteEditPane(noteId = uuid))
                },
                onFabClicked = {
                    navController.navigate(route = NoteMarkDestination.NoteCreatePane)
                },
                onSettingsClicked = {
                    navController.navigate(route = NoteMarkDestination.SettingsPane)
                },
                onProfileClicked = {}
            )
        }

        composable<NoteMarkDestination.NoteCreatePane> {
            val context = LocalActivity.current
            SideEffect { context?.setDarkStatusBarIcons(true) }

            val addNotePresenter:AddNotePresenter = remember { get(clazz = AddNotePresenter::class.java) }
            var addNoteUiModel by remember { mutableStateOf(AddNoteUiModel.Empty) }
            val addNoteAction = addNotePresenter::processEvent

            val scope = rememberCoroutineScope()

            LifecycleStartEffect(key1 = Unit) {
                scope.launchMolecule(mode = RecompositionMode.Immediate) {
                    addNoteUiModel = addNotePresenter.uiModel()
                }
                onStopOrDispose {
                    scope.cancel()
                }
            }

            /**
             * To make sure the collection from AppState machine is canceled
             * Otherwise FlowRedux will throw an exception
             * */
            BackHandler(
                enabled = true,
            ) {
                navController.navigateUp()
            }

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
            val editNotePresenter: EditNotePresenter = remember { get(clazz = EditNotePresenter::class.java) }
            var editNoteUiModel by remember { mutableStateOf(EditNoteUiModel.Empty) }
            val editNoteAction = editNotePresenter::processEvent

            val scope = rememberCoroutineScope()

            LifecycleStartEffect(key1 = Unit) {
                scope.launchMolecule(mode = RecompositionMode.Immediate) {
                    editNoteUiModel = editNotePresenter.uiModel()
                }
                onStopOrDispose {
                    scope.cancel()
                }
            }

            /**
             * To make sure the collection from AppState machine is canceled
             * Otherwise FlowRedux will throw an exception
             * */
            BackHandler(
                enabled = true,
            ) {
                navController.navigateUp()
            }

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

            val settingsPresenter: SettingsPresenter = remember { get(clazz = SettingsPresenter::class.java) }
            var settingsUiModel by remember { mutableStateOf(SettingsUiModel.Empty) }
            val settingsAction = settingsPresenter::processEvent

            val scope = rememberCoroutineScope()
            LifecycleStartEffect(key1 = Unit) {
                scope.launchMolecule(mode = RecompositionMode.Immediate) {
                    settingsUiModel = settingsPresenter.uiModel()
                }
                onStopOrDispose {
                    scope.cancel()
                }
            }

            /**
             * To make sure the collection from AppState machine is canceled
             * Otherwise FlowRedux will throw an exception
             * */
            BackHandler(
                enabled = true,
            ) {
                navController.navigateUp()
            }

            SettingsPane(
                modifier = Modifier,
                settingsUiModel = settingsUiModel,
                settingsAction = settingsAction,
                onBackClicked = { navController.navigateUp() },
                onLogoutSuccessful = {
                    navController.navigate(route = NoteMarkDestination.LauncherPane) {
                        launchSingleTop = true
                    }
                },
                onDeleteNoteCheckChanged = {
                    settingsAction(AppAction.DeleteLocalNotesOnLogout(deleteOnLogout = !settingsUiModel.deleteLocalNotesOnLogout))
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
        val noteId: String
    )

    @Serializable
    data object SettingsPane
}