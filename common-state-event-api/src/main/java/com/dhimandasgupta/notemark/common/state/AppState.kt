package com.dhimandasgupta.notemark.common.state

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.android.ConnectionState
import kotlinx.collections.immutable.ImmutableList

@Immutable
interface AppState {
  val connectionState: ConnectionState

  /** Events waiting to be consumed, oldest first. */
  val appEvents: ImmutableList<Event>
}
