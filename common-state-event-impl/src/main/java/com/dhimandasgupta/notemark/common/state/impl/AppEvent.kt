package com.dhimandasgupta.notemark.common.state.impl

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.state.Event
import kotlin.uuid.Uuid

@Immutable
sealed interface AppEvent : Event {
  data class NetworkAvailable(override val id: Uuid) : AppEvent

  data class NetworkUnAvailable(override val id: Uuid) : AppEvent
}
