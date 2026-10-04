package com.dhimandasgupta.notemark.common.state

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.android.ConnectionState

@Immutable
interface AppState {
  val connectionState: ConnectionState
}
