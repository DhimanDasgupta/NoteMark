package com.dhimandasgupta.notemark

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
import com.dhimandasgupta.notemark.ui.screens.LauncherPane
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

private fun NavGraphBuilder.NoteMarkGraph(
    navController: NavHostController
) {
    navigation<NoteMarkDestination.RootDestination>(
        startDestination = NoteMarkDestination.LauncherDestination
    ) {
        composable<NoteMarkDestination.LauncherDestination> {
            LauncherPane()
        }
    }
}

object NoteMarkDestination {
    @Serializable
    data object RootDestination

    @Serializable
    data object LauncherDestination
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