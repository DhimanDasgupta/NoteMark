package com.dhimandasgupta.notemark.common.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface PreLoginNavKey : NavKey

interface PostLoginNavKey : NavKey

@Serializable data object LauncherNavKey : PreLoginNavKey

@Serializable data object LoginNavKey : PreLoginNavKey

@Serializable data object RegistrationNavKey : PreLoginNavKey

@Serializable data object NoteListNavKey : PostLoginNavKey

@Serializable data object NoteCreateNavKey : PostLoginNavKey

@Serializable data class NoteEditNavKey(val noteId: String) : PostLoginNavKey

@Serializable data object SettingsNavKey : PostLoginNavKey
