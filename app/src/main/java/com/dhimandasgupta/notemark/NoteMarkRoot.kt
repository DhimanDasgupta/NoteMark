package com.dhimandasgupta.notemark

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.dhimandasgupta.notemark.ui.common.getDeviceLayoutType
import com.dhimandasgupta.notemark.ui.screens.LauncherPane
import com.dhimandasgupta.notemark.ui.screens.LoginPane
import com.dhimandasgupta.notemark.ui.screens.RegistrationPane
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Composable
fun NoteMarkRoot(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NoteMarkDestination.RootDestination,
        modifier = modifier
    ) {
        NoteMarkGraph(
            navController = navController
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
private fun NavGraphBuilder.NoteMarkGraph(
    navController: NavHostController
) {
    navigation<NoteMarkDestination.RootDestination>(
        startDestination = NoteMarkDestination.LauncherDestination
    ) {
        composable<NoteMarkDestination.LauncherDestination> {
            LauncherPane(
                navigateToAfterLogin = {},
                navigateToLogin = {
                    navController.navigate(NoteMarkDestination.LoginDestination)
                }
            )
        }

        composable<NoteMarkDestination.LoginDestination> {
            val actualActivity = LocalActivity.current
            val windowSizeClass = calculateWindowSizeClass(actualActivity as Activity)
            LoginPane(
                windowSizeClass = windowSizeClass,
                navigateToAfterLogin = {},
                navigateToRegistration = {
                    navController.navigate(NoteMarkDestination.RegistrationDestination)
                }
            )
        }

        composable<NoteMarkDestination.RegistrationDestination> {
            val actualActivity = LocalActivity.current
            val windowSizeClass = calculateWindowSizeClass(actualActivity as Activity)
            RegistrationPane(
                windowSizeClass = windowSizeClass,
                navigateToLogin = {
                    navController.navigate(NoteMarkDestination.LoginDestination)
                },
                navigateToRegistration = {}
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
}

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedKoinViewModel(
    navController: NavHostController
): T {
    val navGraphRoute = destination.parent?.route ?: koinViewModel<T>()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return koinViewModel<T>(viewModelStoreOwner = parentEntry)
}