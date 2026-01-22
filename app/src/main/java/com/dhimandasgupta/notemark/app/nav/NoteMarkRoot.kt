package com.dhimandasgupta.notemark.app.nav

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
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
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.get

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
                NoteCreateEntry(
                    modifier = modifier,
                    navigateUp = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<NoteEditNavKey>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                NoteEditEntry(
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

@Composable
private fun LauncherEntry(
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

    LauncherPane(
        modifier = modifier,
        launcherUiModel = { launcherUiModel },
        navigateToAfterLogin = {
            if (launcherUiModel.loggedInUser == null) {
                Toast.makeText(
                    context,
                    "Oops!!! Please login first to get started",
                    Toast.LENGTH_LONG
                ).show()
                return@LauncherPane
            }
            navigateAfterLogin()
        },
        navigateToLogin = { navigateToLogin() },
        navigateToList = { navigateAfterLogin() }
    )
}

@Composable
private fun LoginEntry(
    modifier: Modifier = Modifier,
    navigateToRegistration: () -> Unit,
    navigateToAfterLogin: () -> Unit
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

    LoginPane(
        modifier = modifier,
        loginUiModel = { loginUiModel },
        loginAction = { event -> loginEvents(event) },
        navigateToRegistration = { navigateToRegistration() },
        navigateToAfterLogin = { navigateToAfterLogin() },
    )
}

@Composable
private fun RegistrationEntry(
    modifier: Modifier = Modifier,
    navigateToLoginFromRegistration: () -> Unit
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

    RegistrationPane(
        modifier = modifier,
        registrationUiModel = { registrationUiModel },
        navigateToLogin = { navigateToLoginFromRegistration() },
        registrationAction = { event -> registrationAction(event) },
    )
}

@Composable
private fun NoteListEntry(
    modifier: Modifier = Modifier,
    navigateToAdd: () -> Unit,
    navigateToEdit: (String) -> Unit,
    navigateToSettings: () -> Unit
) {
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
private fun NoteCreateEntry(
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

    AddNotePane(
        modifier = modifier,
        addNoteUiModel = { addNoteUiModel },
        addNoteAction = { event -> addNoteAction(event) },
        onBackClicked = { navigateUp() }
    )
}

@Composable
private fun NoteEditEntry(
    modifier: Modifier = Modifier,
    argument: String,
    navigateUp: () -> Unit
) {
    val editNotePresenter: EditNotePresenter = retain {
        get(
            clazz = EditNotePresenter::class.java,
            parameters = { parametersOf(argument) }
        )
    }
    var editNoteUiModel by remember { mutableStateOf(value = EditNoteUiModel.Empty) }
    val editNoteAction by rememberUpdatedState(newValue = editNotePresenter::processEvent)

    val scope = rememberCoroutineScope()

    LifecycleStartEffect(key1 = argument) {
        if (scope.isActive) {
            scope.launchMolecule(mode = RecompositionMode.Immediate) {
                editNoteUiModel = editNotePresenter.uiModel()
            }
        }
        onStopOrDispose {
            scope.cancel()
        }
    }

    EditNotePane(
        modifier = modifier,
        editNoteUiModel = { editNoteUiModel },
        editNoteAction = { event -> editNoteAction(event) },
        onCloseClicked = { navigateUp() }
    )
}

@Composable
private fun SettingsEntry(
    modifier: Modifier = Modifier,
    navigateToLauncherAfterLogout: () -> Unit,
    navigateUp: () -> Unit
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