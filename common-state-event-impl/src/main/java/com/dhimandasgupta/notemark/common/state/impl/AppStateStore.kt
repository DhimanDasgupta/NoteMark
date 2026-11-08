package com.dhimandasgupta.notemark.common.state.impl

import com.dhimandasgupta.notemark.common.state.AppAction
import com.dhimandasgupta.notemark.common.state.AppState
import com.dhimandasgupta.notemark.core.di.AppBackgroundScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * The one app-wide [AppState] machine. Every screen reads [state] and sends actions through
 * [dispatch]. That way they all see the same state, and an event consumed on one screen is gone for
 * the others too.
 *
 * The machine starts when [state] is first collected and runs for as long as [scope] does.
 */
@SingleIn(AppScope::class)
@Inject
class AppStateStore(
  appStateMachineFactory: AppStateMachineFactory,
  @AppBackgroundScope scope: CoroutineScope,
) {
  private val appStateMachine = appStateMachineFactory.launchIn(scope = scope)

  val state: StateFlow<AppState>
    get() = appStateMachine.state

  suspend fun dispatch(action: AppAction) = appStateMachine.dispatch(action = action)
}
