package com.dhimandasgupta.notemark.app.nav

import androidx.annotation.Keep
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface PreLoginNavKey: NavKey

interface PostLoginNavKey: NavKey

@Keep
@Serializable
data object LauncherNavKey: PreLoginNavKey

@Keep
@Serializable
data object LoginNavKey: PreLoginNavKey

@Keep
@Serializable
data object RegistrationNavKey: PreLoginNavKey

@Keep
@Serializable
data object NoteListNavKey: PostLoginNavKey

@Keep
@Serializable
data object NoteCreateNavKey: PostLoginNavKey

@Keep
@Serializable
data class NoteEditNavKey(
    val noteId: String
): PostLoginNavKey

@Keep
@Serializable
data object SettingsNavKey: PostLoginNavKey