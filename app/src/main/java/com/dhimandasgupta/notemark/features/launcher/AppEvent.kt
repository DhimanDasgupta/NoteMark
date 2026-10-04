package com.dhimandasgupta.notemark.features.launcher

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.state.Event
import kotlin.uuid.Uuid

@Immutable
sealed interface AppEvent : Event {
  data class NetworkAvailable(@all:Override override val id: Uuid) : AppEvent

  data class NetworkUnAvailable(@all:Override override val id: Uuid) : AppEvent
}
