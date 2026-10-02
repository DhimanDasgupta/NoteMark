package com.dhimandasgupta.notemark.features.launcher

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.extensions.android.ConnectionState
import com.dhimandasgupta.notemark.common.extensions.android.addCreateNewNoteShortcut
import com.dhimandasgupta.notemark.common.extensions.android.cancelPeriodicSync
import com.dhimandasgupta.notemark.common.extensions.android.cancelSyncWork
import com.dhimandasgupta.notemark.common.extensions.android.getAppVersionName
import com.dhimandasgupta.notemark.common.extensions.android.observeConnectivityAsFlow
import com.dhimandasgupta.notemark.common.extensions.android.observeSyncRunning
import com.dhimandasgupta.notemark.common.extensions.android.removeCreateNewNoteShortcut
import com.dhimandasgupta.notemark.common.extensions.android.schedulePeriodicSync
import com.dhimandasgupta.notemark.common.extensions.android.triggerOneTimeSync
import com.dhimandasgupta.notemark.common.extensions.kotlin.getDifferenceFromTimestampInMinutes
import com.dhimandasgupta.notemark.common.extensions.kotlin.parseIsoInstantOrNull
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.SyncRepository
import com.dhimandasgupta.notemark.data.UserRepository
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.proto.Sync
import com.dhimandasgupta.notemark.proto.User
import com.freeletics.flowredux2.FlowReduxStateMachineFactory as StateMachineFactory
import com.freeletics.flowredux2.initializeWith
import dev.zacsweers.metro.Inject
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.withContext

@Immutable
sealed interface AppState {
  val connectionState: ConnectionState

  data class NotLoggedIn(
    override val connectionState: ConnectionState = ConnectionState.Unavailable
  ) : AppState

  data class LoggedIn(
    override val connectionState: ConnectionState = ConnectionState.Unavailable,
    val appEvents: ImmutableList<AppEvent> = persistentListOf(),
    val user: User,
    val sync: Sync? = null,
    val isSyncing: Boolean = false,
    val appVersionName: String,
  ) : AppState
}

sealed interface AppAction {
  data class UpdateSync(val syncDuration: Sync.SyncDuration) : AppAction

  object AppLogout : AppAction

  object SyncNow : AppAction

  data class DeleteLocalNotesOnLogout(val deleteOnLogout: Boolean) : AppAction

  data class AppEventConsumed(val id: Uuid) : AppAction
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
          flow = applicationContext.observeConnectivityAsFlow().distinctUntilChanged().withIndex()
        ) { (index, connected) ->
          mutate {
            // The first emission is the current state on entering LoggedIn, not a change
            if (index == 0 || connected == connectionState)
              return@mutate copy(connectionState = connected)
            val event =
              when (connected) {
                ConnectionState.Available -> AppEvent.NetworkAvailable(id = Uuid.random())
                ConnectionState.Unavailable -> AppEvent.NetworkUnAvailable(id = Uuid.random())
              }
            copy(connectionState = connected, appEvents = (appEvents + event).toImmutableList())
          }
        }
        collectWhileInState(flow = syncRepository.getSync()) { sync ->
          mutate { copy(sync = sync) }
        }
        collectWhileInState(flow = applicationContext.observeSyncRunning()) { isSyncing ->
          mutate { copy(isSyncing = isSyncing) }
        }
        collectWhileInState(flow = userRepository.getUser().distinctUntilChanged()) { user ->
          user?.let {
            noChange()
          } ?: override { AppState.NotLoggedIn(connectionState = connectionState) }
        }

        // All the actions valid for app state should be handled here
        on<AppAction.UpdateSync> { action ->
          val interval =
            when (action.syncDuration) {
              Sync.SyncDuration.SYNC_DURATION_FIFTEEN_MINUTES -> 15.minutes
              Sync.SyncDuration.SYNC_DURATION_THIRTY_MINUTES -> 30.minutes
              Sync.SyncDuration.SYNC_DURATION_ONE_HOUR -> 1.hours
              else -> null
            }

          when (interval) {
            null -> applicationContext.cancelPeriodicSync()
            else -> applicationContext.schedulePeriodicSync(interval = interval)
          }
          syncRepository.saveSyncDuration(syncDuration = action.syncDuration)

          mutate {
            val sync = copy().sync?.toBuilder()?.setSyncDuration(action.syncDuration)?.build()
            copy(sync = sync)
          }
        }
        onActionEffect<AppAction.SyncNow> { _ -> applicationContext.triggerOneTimeSync() }
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
        on<AppAction.AppEventConsumed> { action ->
          mutate {
            copy(
              appEvents =
                appEvents.filterNot { appEvent -> action.id == appEvent.id }.toImmutableList()
            )
          }
        }
      }
    }
  }

  companion object Companion {
    val defaultAppState = AppState.NotLoggedIn()
  }

  private suspend fun onLogoutSuccessful(deleteLocalNotesOnLogout: Boolean = false) =
    withContext(NonCancellable) {
      applicationContext.cancelSyncWork()
      if (deleteLocalNotesOnLogout) {
        noteMarkRepository.deleteAllLocalNotes()
      }
      syncRepository.reset()
      userRepository.reset()
    }

  private suspend fun syncOnEnter() {
    val lastUploadedTime = syncRepository.getSync().first().lastUploadedTime
    val neverSynced = parseIsoInstantOrNull(isoOffsetDateTimeString = lastUploadedTime) == null
    val lastSyncTimeIsMoreThan5Minutes =
      getDifferenceFromTimestampInMinutes(isoOffsetDateTimeString = lastUploadedTime) > 5L
    // A sync that is already queued or running is kept, so this never starts a second one.
    if (neverSynced || lastSyncTimeIsMoreThan5Minutes) {
      applicationContext.triggerOneTimeSync()
    }
  }
}
