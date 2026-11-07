package com.dhimandasgupta.notemark.common.state.impl

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.state.Event
import kotlin.uuid.Uuid

@Immutable
sealed interface AppEvent : Event {
  data class NetworkAvailable(override val id: Uuid) : AppEvent

  data class NetworkUnAvailable(override val id: Uuid) : AppEvent

  /** A user has logged in. The app moves to [LoggedIn] only once this event is consumed. */
  data class Login(override val id: Uuid) : AppEvent

  /** The user has logged out. The app moves to [NotLoggedIn] only once this event is consumed. */
  data class Logout(override val id: Uuid) : AppEvent
}
