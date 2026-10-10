package com.dhimandasgupta.notemark.app.nav

import androidx.annotation.Keep
import com.dhimandasgupta.notemark.common.navigation.PostLoginNavKey
import com.dhimandasgupta.notemark.common.navigation.PreLoginNavKey
import kotlinx.serialization.Serializable

@Keep @Serializable data object RegistrationNavKey : PreLoginNavKey

@Keep @Serializable data object NoteListNavKey : PostLoginNavKey

@Keep @Serializable data object NoteCreateNavKey : PostLoginNavKey

@Keep @Serializable data class NoteEditNavKey(val noteId: String) : PostLoginNavKey

@Keep @Serializable data object SettingsNavKey : PostLoginNavKey
