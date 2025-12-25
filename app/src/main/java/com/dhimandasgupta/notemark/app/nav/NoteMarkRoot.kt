package com.dhimandasgupta.notemark.app.nav

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
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
import kotlinx.coroutines.isActive
import kotlinx.serialization.Serializable
import org.koin.java.KoinJavaComponent.get

@Composable
fun NoteMarkRoot(
    navController: NavHostController,
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
        noteMarkGraph(navController = navController)
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
private fun NavGraphBuilder.noteMarkGraph(
    navController: NavHostController
) {
    navigation<NoteMarkDestination.RootPane>(
        startDestination = NoteMarkDestination.LauncherPane
    ) {
        composable<NoteMarkDestination.LauncherPane> {
            LauncherPane(
                modifier = Modifier,
                navigateAfterLogin = navController::navigateAfterLogin,
                navigateToLogin = navController::navigateToLogin
            )
        }

        composable<NoteMarkDestination.LoginPane> {
            LoginPane(
                modifier = Modifier,
                navigateToRegistration = navController::navigateToRegistration,
                navigateToAfterLogin = navController::navigateToAfterLogin,
                navigateUp = navController::navigateAppUp
            )
        }

        composable<NoteMarkDestination.RegistrationPane> {
            RegistrationPane(
                modifier = Modifier,
                navigateToLoginFromRegistration = navController::navigateToLoginFromRegistration,
                navigateUp = navController::navigateAppUp
            )
        }

        composable<NoteMarkDestination.NoteListPane> {
            NoteListPane(
                modifier = Modifier,
                navigateToAdd = navController::navigateToAdd,
                navigateToEdit = navController::navigateToEdit,
                navigateToSettings = navController::navigateToSettings
            )
        }

        composable<NoteMarkDestination.NoteCreatePane> {
            NoteCreatePane(
                modifier = Modifier,
                navigateUp = navController::navigateAppUp
            )
        }

        composable<NoteMarkDestination.NoteEditPane> { backStackEntry ->
            val arguments: NoteMarkDestination.NoteEditPane = backStackEntry.toRoute()

            NoteEditPane(
                modifier = Modifier,
                arguments = arguments,
                navigateUp = navController::navigateAppUp
            )
        }

        composable<NoteMarkDestination.SettingsPane> {
            SettingsPane(
                modifier = Modifier,
                navigateUp = navController::navigateAppUp,
                navigateToLauncherAfterLogout = navController::navigateToLauncherAfterLogout
            )
        }
    }
}

@Composable
private fun LauncherPane(
    modifier: Modifier = Modifier,
    navigateAfterLogin: () -> Unit,
    navigateToLogin: () -> Unit
) {
    val context = LocalActivity.current

    val launcherPresenter: LauncherPresenter = retain { get(clazz = LauncherPresenter::class.java) }
    var launcherUiModel by remember { mutableStateOf(value = LauncherUiModel.Empty) }

    val scope = rememberCoroutineScope()
    LifecycleStartEffect(key1 = Unit) {
        if (scope.isActive) {
            scope.launchMolecule(mode = RecompositionMode.Immediate) {
                launcherUiModel = launcherPresenter.uiModel()
            }
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
        modifier = modifier,
        launcherUiModel = { launcherUiModel },
        navigateToAfterLogin = {
            if (launcherUiModel.loggedInUser == null) {
                Toast.makeText(context, "Oops!!! Please login first to get started", Toast.LENGTH_LONG).show()
                return@LauncherPane
            }
            navigateAfterLogin()
        },
        navigateToLogin = { navigateToLogin() },
        navigateToList = { navigateAfterLogin() }
    )
}

@Composable
private fun LoginPane(
    modifier: Modifier = Modifier,
    navigateToRegistration: () -> Unit,
    navigateToAfterLogin: () -> Unit,
    navigateUp: () -> Unit
) {
    val loginPresenter: LoginPresenter = retain { get(clazz = LoginPresenter::class.java) }
    var loginUiModel by remember { mutableStateOf(value = LoginUiModel.Empty) }
    val loginEvents by rememberUpdatedState(newValue = loginPresenter::processEvent)

    val scope = rememberCoroutineScope()
    LifecycleStartEffect(key1 = Unit) {
        if (scope.isActive) {
            scope.launchMolecule(mode = RecompositionMode.Immediate) {
                loginUiModel = loginPresenter.uiModel()
            }
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
        navigateUp()
    }

    LoginPane(
        modifier = modifier,
        loginUiModel = { loginUiModel },
        loginAction = { event -> loginEvents(event) },
        navigateToRegistration = { navigateToRegistration() },
        navigateToAfterLogin = { navigateToAfterLogin() },
    )
}

@Composable
private fun RegistrationPane(
    modifier: Modifier = Modifier,
    navigateToLoginFromRegistration: () -> Unit,
    navigateUp: () -> Unit
) {
    val registrationPresenter: RegistrationPresenter = retain { get(clazz = RegistrationPresenter::class.java) }
    var registrationUiModel by remember { mutableStateOf(value = RegistrationUiModel.Empty) }
    val registrationAction by rememberUpdatedState(newValue = registrationPresenter::processEvent)

    val scope = rememberCoroutineScope()
    LifecycleStartEffect(key1 = Unit) {
        if (scope.isActive) {
            scope.launchMolecule(mode = RecompositionMode.Immediate) {
                registrationUiModel = registrationPresenter.uiModel()
            }
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
        navigateUp()
    }

    RegistrationPane(
        modifier = modifier,
        registrationUiModel = { registrationUiModel },
        navigateToLogin = { navigateToLoginFromRegistration() },
        registrationAction = { event -> registrationAction(event) },
    )
}

@Composable
private fun NoteListPane(
    modifier: Modifier = Modifier,
    navigateToAdd: () -> Unit,
    navigateToEdit: (String) -> Unit,
    navigateToSettings: () -> Unit
) {
    val context = LocalActivity.current

    val noteListPresenter: NoteListPresenter = retain { get(clazz = NoteListPresenter::class.java) }
    var noteListUiModel by remember { mutableStateOf(value = NoteListUiModel.Empty) }
    val noteListAction by rememberUpdatedState(newValue = noteListPresenter::processEvent)

    val scope = rememberCoroutineScope()
    LifecycleStartEffect(key1 = Unit) {
        if (scope.isActive) {
            scope.launchMolecule(mode = RecompositionMode.Immediate) {
                noteListUiModel = noteListPresenter.uiModel()
            }
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
        context?.finish()
    }

    NoteListPane(
        modifier = modifier,
        noteListUiModel = { noteListUiModel },
        noteListAction = { event -> noteListAction(event) },
        onNoteClicked = { uuid -> navigateToEdit(uuid) },
        onFabClicked = { navigateToAdd() },
        onSettingsClicked = { navigateToSettings() },
        onProfileClicked = {}
    )
}

@Composable
private fun NoteCreatePane(
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit
) {
    val addNotePresenter:AddNotePresenter = retain { get(clazz = AddNotePresenter::class.java) }
    var addNoteUiModel by remember { mutableStateOf(value = AddNoteUiModel.Empty) }
    val addNoteAction by rememberUpdatedState(newValue = addNotePresenter::processEvent)

    val scope = rememberCoroutineScope()

    LifecycleStartEffect(key1 = Unit) {
        if (scope.isActive) {
            scope.launchMolecule(mode = RecompositionMode.Immediate) {
                addNoteUiModel = addNotePresenter.uiModel()
            }
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
        navigateUp()
    }

    AddNotePane(
        modifier = modifier,
        addNoteUiModel = { addNoteUiModel },
        addNoteAction = { event -> addNoteAction(event) },
        onBackClicked = { navigateUp() }
    )
}

@Composable
private fun NoteEditPane(
    modifier: Modifier = Modifier,
    arguments: NoteMarkDestination.NoteEditPane,
    navigateUp: () -> Unit
) {
    val editNotePresenter: EditNotePresenter = retain { get(clazz = EditNotePresenter::class.java) }
    var editNoteUiModel by remember { mutableStateOf(value = EditNoteUiModel.Empty) }
    val editNoteAction by rememberUpdatedState(newValue = editNotePresenter::processEvent)

    val scope = rememberCoroutineScope()

    LifecycleStartEffect(key1 = arguments.noteId) {
        if (scope.isActive) {
            scope.launchMolecule(mode = RecompositionMode.Immediate) {
                editNoteUiModel = editNotePresenter.uiModel()
            }
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
        navigateUp()
    }

    EditNotePane(
        modifier = modifier,
        noteId = arguments.noteId,
        editNoteUiModel = { editNoteUiModel },
        editNoteAction = { event -> editNoteAction(event) },
        onCloseClicked = { navigateUp() }
    )
}

@Composable
private fun SettingsPane(
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit,
    navigateToLauncherAfterLogout: () -> Unit
) {
    val settingsPresenter: SettingsPresenter = retain { get(clazz = SettingsPresenter::class.java) }
    var settingsUiModel by remember { mutableStateOf(value =  SettingsUiModel.Empty) }
    val settingsAction by rememberUpdatedState(newValue = settingsPresenter::processEvent)

    val scope = rememberCoroutineScope()
    LifecycleStartEffect(key1 = Unit) {
        if (scope.isActive) {
            scope.launchMolecule(mode = RecompositionMode.Immediate) {
                settingsUiModel = settingsPresenter.uiModel()
            }
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
        navigateUp()
    }

    SettingsPane(
        modifier = modifier,
        settingsUiModel = { settingsUiModel },
        settingsAction = { event -> settingsAction(event) },
        onBackClicked = { navigateUp() },
        onLogoutSuccessful = { navigateToLauncherAfterLogout() },
        onDeleteNoteCheckChanged = {
            settingsAction(AppAction.DeleteLocalNotesOnLogout(deleteOnLogout = !settingsUiModel.deleteLocalNotesOnLogout))
        },
        onLogoutClicked = {
            settingsAction(AppAction.AppLogout)
        }
    )
}

private fun NavHostController.navigateToLogin() = navigate(route = NoteMarkDestination.LoginPane) {
    popUpTo(route = NoteMarkDestination.LauncherPane) {
        inclusive = true
    }
}

private fun NavHostController.navigateAfterLogin() = navigate(route = NoteMarkDestination.NoteListPane) {
    popUpTo(route = NoteMarkDestination.LauncherPane) {
        inclusive = true
    }
}

/**
 * Start of Navigation routes
 * */
private object NoteMarkDestination {
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
/**
 * End of Navigation routes
 * */

/**
 * Start of all navigation functions
 * */
private fun NavHostController.navigateToRegistration() = navigate(route = NoteMarkDestination.RegistrationPane) {
    popUpTo(route = NoteMarkDestination.LoginPane) {
        inclusive = true
    }
}

private fun NavHostController.navigateToAfterLogin() = navigate(route = NoteMarkDestination.NoteListPane) {
    popUpTo(route = NoteMarkDestination.LoginPane) {
        inclusive = true
    }
}

private fun NavHostController.navigateToLoginFromRegistration() = navigate(route = NoteMarkDestination.LoginPane) {
    popUpTo(route = NoteMarkDestination.RegistrationPane) {
        inclusive = true
    }
}

private fun NavHostController.navigateToAdd() = navigate(route = NoteMarkDestination.NoteCreatePane)

private fun NavHostController.navigateToEdit(uuid: String) {
    navigate(route = NoteMarkDestination.NoteEditPane(noteId = uuid))
}

private fun NavHostController.navigateToSettings() = navigate(route = NoteMarkDestination.SettingsPane)

private fun NavHostController.navigateToLauncherAfterLogout() = navigate(route = NoteMarkDestination.LauncherPane) {
    launchSingleTop = true
}

private fun NavHostController.navigateAppUp() = navigateUp()
/**
 * End of all navigation functions
 * */