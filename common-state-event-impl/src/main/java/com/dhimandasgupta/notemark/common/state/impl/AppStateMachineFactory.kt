package com.dhimandasgupta.notemark.common.state.impl

import android.app.Application
import android.content.Context
import com.dhimandasgupta.notemark.common.android.AppShortcuts
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.common.android.ConnectivityObserver
import com.dhimandasgupta.notemark.common.android.SyncScheduler
import com.dhimandasgupta.notemark.common.android.getAppVersionName
import com.dhimandasgupta.notemark.common.extensions.kotlin.getDifferenceFromTimestampInMinutes
import com.dhimandasgupta.notemark.common.extensions.kotlin.parseIsoInstantOrNull
import com.dhimandasgupta.notemark.common.state.AppAction
import com.dhimandasgupta.notemark.common.state.AppState
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.data.repository.NoteMarkRepository
import com.dhimandasgupta.notemark.data.repository.SyncRepository
import com.dhimandasgupta.notemark.data.repository.UserRepository
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

data class NotLoggedIn(
  override val connectionState: ConnectionState = ConnectionState.Unavailable,
  override val appEvents: ImmutableList<AppEvent> = persistentListOf(),
) : AppState

data class LoggedIn(
  override val connectionState: ConnectionState = ConnectionState.Unavailable,
  override val appEvents: ImmutableList<AppEvent> = persistentListOf(),
  val user: User,
  val sync: Sync? = null,
  val isSyncing: Boolean = false,
  val appVersionName: String,
) : AppState

data class UpdateSync(val syncDuration: Sync.SyncDuration) : AppAction

object AppLogout : AppAction

object SyncNow : AppAction

data class DeleteLocalNotesOnLogout(val deleteOnLogout: Boolean) : AppAction

data class AppEventConsumed(val appEvent: AppEvent) : AppAction

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AppStateMachineFactory(
  private val applicationContext: Context,
  private val userRepository: UserRepository,
  private val syncRepository: SyncRepository,
  private val noteMarkRepository: NoteMarkRepository,
  private val syncScheduler: SyncScheduler,
  private val appShortcuts: AppShortcuts,
  private val connectivityObserver: ConnectivityObserver,
) : StateMachineFactory<AppState, AppAction>() {
  init {
    require(value = applicationContext is Application) { "Context must be an Application" }

    spec {
      initializeWith { defaultAppState }

      inState<NotLoggedIn> {
        onEnterEffect { appShortcuts.removeCreateNewNoteShortcut() }
        collectWhileInState(flow = userRepository.getUser().distinctUntilChanged().withIndex()) {
          (index, user) ->
          when {
            // A login that has not been consumed yet is withdrawn once the user is gone again
            user == null -> mutate { copy(appEvents = appEvents.withoutPending<AppEvent.Login>()) }
            // The first emission is the session stored before this machine started, not a login
            index == 0 -> override { loggedIn(user = user, connectionState = connectionState) }
            else ->
              mutate {
                copy(appEvents = appEvents.withPending { AppEvent.Login(id = Uuid.random()) })
              }
          }
        }
        collectWhileInState(flow = connectivityObserver.observe().distinctUntilChanged()) {
          connected ->
          mutate { copy(connectionState = connected) }
        }

        on<AppEventConsumed> { action ->
          val event = action.appEvent
          when {
            snapshot.appEvents.none { appEvent -> appEvent.id == event.id } -> noChange()
            event is AppEvent.Login ->
              userRepository.getUser().first()?.let { user ->
                override { loggedIn(user = user, connectionState = connectionState) }
              } ?: mutate { copy(appEvents = appEvents.without(event = event)) }
            else -> mutate { copy(appEvents = appEvents.without(event = event)) }
          }
        }
      }

      inState<LoggedIn> {
        onEnterEffect {
          syncOnEnter()
          appShortcuts.addCreateNewNoteShortcut()
        }
        collectWhileInState(
          flow = connectivityObserver.observe().distinctUntilChanged().withIndex()
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
        collectWhileInState(flow = syncScheduler.observeSyncRunning()) { isSyncing ->
          mutate { copy(isSyncing = isSyncing) }
        }
        collectWhileInState(flow = userRepository.getUser().distinctUntilChanged()) { user ->
          mutate {
            copy(
              appEvents =
                when (user) {
                  null -> appEvents.withPending { AppEvent.Logout(id = Uuid.random()) }
                  // A logout that has not been consumed yet is withdrawn once a user is back
                  else -> appEvents.withoutPending<AppEvent.Logout>()
                }
            )
          }
        }

        // All the actions valid for app state should be handled here
        on<UpdateSync> { action ->
          val interval =
            when (action.syncDuration) {
              Sync.SyncDuration.SYNC_DURATION_FIFTEEN_MINUTES -> 15.minutes
              Sync.SyncDuration.SYNC_DURATION_THIRTY_MINUTES -> 30.minutes
              Sync.SyncDuration.SYNC_DURATION_ONE_HOUR -> 1.hours
              else -> null
            }

          when (interval) {
            null -> syncScheduler.cancelPeriodicSync()
            else -> syncScheduler.schedulePeriodicSync(interval = interval)
          }
          syncRepository.saveSyncDuration(syncDuration = action.syncDuration)

          mutate {
            val sync = copy().sync?.toBuilder()?.setSyncDuration(action.syncDuration)?.build()
            copy(sync = sync)
          }
        }
        onActionEffect<SyncNow> { _ -> syncScheduler.triggerOneTimeSync() }
        onActionEffect<DeleteLocalNotesOnLogout> { action ->
          syncRepository.saveDeleteLocalNotesOnLogout(
            deleteLocalNotesOnLogout = action.deleteOnLogout
          )
        }
        on<AppLogout> { _ ->
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
              // The cleared user also adds the Logout event, this only covers a slow emission
              mutate {
                copy(appEvents = appEvents.withPending { AppEvent.Logout(id = Uuid.random()) })
              }
            } ?: noChange()
        }
        on<AppEventConsumed> { action ->
          val event = action.appEvent
          when {
            snapshot.appEvents.none { appEvent -> appEvent.id == event.id } -> noChange()
            event is AppEvent.Logout -> override { NotLoggedIn(connectionState = connectionState) }
            else -> mutate { copy(appEvents = appEvents.without(event = event)) }
          }
        }
      }
    }
  }

  companion object Companion {
    val defaultAppState = NotLoggedIn()
  }

  private fun loggedIn(user: User, connectionState: ConnectionState) =
    LoggedIn(
      connectionState = connectionState,
      user = user,
      appVersionName = applicationContext.getAppVersionName(),
    )

  private suspend fun onLogoutSuccessful(deleteLocalNotesOnLogout: Boolean = false) =
    withContext(NonCancellable) {
      syncScheduler.cancelSyncWork()
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
      syncScheduler.triggerOneTimeSync()
    }
  }
}

/**
 * Adds the event made by [create] unless an event of type [T] is already waiting to be consumed.
 */
private inline fun <reified T : AppEvent> ImmutableList<AppEvent>.withPending(
  create: () -> T
): ImmutableList<AppEvent> =
  if (any { appEvent -> appEvent is T }) this else (this + create()).toImmutableList()

private inline fun <reified T : AppEvent> ImmutableList<AppEvent>.withoutPending():
  ImmutableList<AppEvent> =
  if (none { appEvent -> appEvent is T }) this
  else filterNot { appEvent -> appEvent is T }.toImmutableList()

private fun ImmutableList<AppEvent>.without(event: AppEvent): ImmutableList<AppEvent> =
  filterNot { appEvent -> appEvent.id == event.id }.toImmutableList()
