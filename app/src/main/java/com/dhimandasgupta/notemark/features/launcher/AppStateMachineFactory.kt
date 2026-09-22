package com.dhimandasgupta.notemark.features.launcher

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.extensions.android.ConnectionState
import com.dhimandasgupta.notemark.common.extensions.android.addCreateNewNoteShortcut
import com.dhimandasgupta.notemark.common.extensions.android.cancelPreviousAndTriggerNewWork
import com.dhimandasgupta.notemark.common.extensions.android.getAppVersionName
import com.dhimandasgupta.notemark.common.extensions.android.observeConnectivityAsFlow
import com.dhimandasgupta.notemark.common.extensions.android.removeCreateNewNoteShortcut
import com.dhimandasgupta.notemark.common.getDifferenceFromTimestampInMinutes
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.SyncRepository
import com.dhimandasgupta.notemark.data.UserRepository
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.proto.Sync
import com.dhimandasgupta.notemark.proto.User
import com.freeletics.flowredux2.FlowReduxStateMachineFactory as StateMachineFactory
import com.freeletics.flowredux2.initializeWith
import dev.zacsweers.metro.Inject
import java.time.Duration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first

@Immutable
sealed interface AppState {
  val connectionState: ConnectionState

  data class NotLoggedIn(
    override val connectionState: ConnectionState = ConnectionState.Unavailable
  ) : AppState

  data class LoggedIn(
    override val connectionState: ConnectionState = ConnectionState.Unavailable,
    val user: User,
    val sync: Sync? = null,
    val appVersionName: String,
  ) : AppState
}

sealed interface AppAction {
  data class UpdateSync(val syncDuration: Sync.SyncDuration) : AppAction

  object AppLogout : AppAction

  data class DeleteLocalNotesOnLogout(val deleteOnLogout: Boolean) : AppAction
}

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AppStateMachineFactory(
  private val applicationContext: Context,
  private val userRepository: UserRepository,
  private val syncRepository: SyncRepository,
  private val noteMarkRepository: NoteMarkRepository,
) : StateMachineFactory<AppState, AppAction>() {
  init {
    require(value = applicationContext is Application) { "Context must be an Application" }

    spec {
      initializeWith { defaultAppState }

      inState<AppState.NotLoggedIn> {
        onEnterEffect { applicationContext.removeCreateNewNoteShortcut() }
        collectWhileInState(flow = userRepository.getUser().distinctUntilChanged()) { user ->
          user?.let {
            override {
              AppState.LoggedIn(
                connectionState = connectionState,
                user = user,
                appVersionName = applicationContext.getAppVersionName(),
              )
            }
          }
            ?: run {
              noChange()
            }
        }
        collectWhileInState(
          flow = applicationContext.observeConnectivityAsFlow().distinctUntilChanged()
        ) { connected ->
          mutate { copy(connectionState = connected) }
        }
      }

      inState<AppState.LoggedIn> {
        onEnterEffect {
          syncOnEnter()
          applicationContext.addCreateNewNoteShortcut()
        }
        collectWhileInState(
          flow = applicationContext.observeConnectivityAsFlow().distinctUntilChanged()
        ) { connected ->
          mutate { copy(connectionState = connected) }
        }
        collectWhileInState(flow = syncRepository.getSync()) { sync ->
          mutate { copy(sync = sync) }
        }
        collectWhileInState(flow = userRepository.getUser().distinctUntilChanged()) { user ->
          user?.let {
            noChange()
          } ?: override { AppState.NotLoggedIn(connectionState = connectionState) }
        }

        // All the actions valid for app state should be handled here
        on<AppAction.UpdateSync> { action ->
          val duration =
            when (action.syncDuration) {
              Sync.SyncDuration.SYNC_DURATION_FIFTEEN_MINUTES -> Duration.ofMinutes(15)
              Sync.SyncDuration.SYNC_DURATION_THIRTY_MINUTES -> Duration.ofMinutes(30)
              Sync.SyncDuration.SYNC_DURATION_ONE_HOUR -> Duration.ofHours(1)
              else -> Duration.ZERO
            }

          applicationContext.cancelPreviousAndTriggerNewWork(duration = duration)
          syncRepository.saveSyncDuration(syncDuration = action.syncDuration)

          mutate {
            val sync = copy().sync?.toBuilder()?.setSyncDuration(action.syncDuration)?.build()
            copy(sync = sync)
          }
        }
        onActionEffect<AppAction.DeleteLocalNotesOnLogout> { action ->
          syncRepository.saveDeleteLocalNotesOnLogout(
            deleteLocalNotesOnLogout = action.deleteOnLogout
          )
        }
        on<AppAction.AppLogout> { _ ->
          if (snapshot.connectionState == ConnectionState.Unavailable) return@on noChange()

          noteMarkRepository
            .logout(
              request =
                RefreshRequest(refreshToken = userRepository.getUser().first()?.refreshToken ?: "")
            )
            .getOrNull()
            ?.let {
              onLogoutSuccessful(
                deleteLocalNotesOnLogout = syncRepository.getSync().first().deleteLocalNotesOnLogout
              )
              override {
                AppState.NotLoggedIn(connectionState = connectionState)
              }
            } ?: noChange()
        }
      }
    }
  }

  companion object Companion {
    val defaultAppState = AppState.NotLoggedIn()
  }

  private suspend fun onLogoutSuccessful(deleteLocalNotesOnLogout: Boolean = false) {
    if (deleteLocalNotesOnLogout) {
      noteMarkRepository.deleteAllLocalNotes()
    }
    userRepository.reset()
    syncRepository.reset()
  }

  private suspend fun syncOnEnter() {
    val sync = syncRepository.getSync().first()
    val neverSynced =
      sync.lastUploadedTime.isNullOrEmpty() && sync.lastDownloadedTime.isNullOrEmpty()
    val lastSyncTimeIsMoreThan5Minutes =
      getDifferenceFromTimestampInMinutes(isoOffsetDateTimeString = sync.lastUploadedTime) > 5L
    // Start sync if never synced or the last sync time is more than 5 mins and not syncing.
    if (neverSynced || (lastSyncTimeIsMoreThan5Minutes && !sync.syncing)) {
      applicationContext.cancelPreviousAndTriggerNewWork()
    }
  }
}
