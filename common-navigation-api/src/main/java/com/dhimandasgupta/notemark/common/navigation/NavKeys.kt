package com.dhimandasgupta.notemark.common.navigation

import androidx.annotation.Keep
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface PreLoginNavKey : NavKey

interface PostLoginNavKey : NavKey

@Keep @Serializable data object LauncherNavKey : PreLoginNavKey

@Keep @Serializable data object LoginNavKey : PreLoginNavKey
