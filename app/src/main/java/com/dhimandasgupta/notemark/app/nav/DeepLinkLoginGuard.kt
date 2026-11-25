package com.dhimandasgupta.notemark.app.nav

import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.common.navigation.LauncherNavKey
import com.dhimandasgupta.notemark.common.navigation.LoginNavKey
import com.dhimandasgupta.notemark.common.navigation.PostLoginNavKey
import com.dhimandasgupta.notemark.data.repository.UserRepository
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull

/**
 * Sends a logged-out user to the login screen when a deep link needs a logged-in user.
 *
 * It asks [UserRepository] and not the app state, because the app state also starts as not logged
 * in while the stored user is still being loaded.
 */
@Inject
class DeepLinkLoginGuard(private val userRepository: UserRepository) {
  suspend fun guard(backStack: PersistentList<NavKey>): PersistentList<NavKey> =
    if (backStack.none { navKey -> navKey is PostLoginNavKey } || isLoggedIn()) backStack
    else persistentListOf(LauncherNavKey, LoginNavKey)

  // A user that can't be read counts as logged out
  private suspend fun isLoggedIn(): Boolean =
    userRepository.getUser().catch { emit(value = null) }.firstOrNull() != null
}
