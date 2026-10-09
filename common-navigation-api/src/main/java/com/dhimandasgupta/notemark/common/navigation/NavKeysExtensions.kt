package com.dhimandasgupta.notemark.common.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun NavBackStack<NavKey>.clearPreLoginKeys(): Boolean = removeAll { navKey ->
  navKey is PreLoginNavKey
}

fun NavBackStack<NavKey>.clearPostLoginNavKeys(): Boolean = removeAll { navKey ->
  navKey is PostLoginNavKey
}
