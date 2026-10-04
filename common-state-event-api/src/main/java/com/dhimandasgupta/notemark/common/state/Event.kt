package com.dhimandasgupta.notemark.common.state

import androidx.compose.runtime.Immutable
import kotlin.uuid.Uuid

@Immutable
interface Event {
  val id: Uuid
}
