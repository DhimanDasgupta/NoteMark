package com.dhimandasgupta.notemark.features.launcher

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dhimandasgupta.notemark.app.work.NoteSyncWorker
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.common.android.observeConnectivityAsFlow
import com.dhimandasgupta.notemark.common.getDifferenceFromTimestampInMinutes
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.SyncRepository
import com.dhimandasgupta.notemark.data.UserRepository
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.proto.Sync
import com.dhimandasgupta.notemark.proto.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import java.time.Duration
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine

@Immutable
sealed interface AppState {
    val connectionState: ConnectionState?

    data class NotLoggedIn(
        override val connectionState: ConnectionState? = ConnectionState.Unavailable
    ) : AppState

    data class LoggedIn(
        override val connectionState: ConnectionState? = ConnectionState.Unavailable,
        val user: User,
        val sync: Sync? = null,
    ) : AppState
}

sealed interface AppAction {
    object ConnectionStateConsumed: AppAction
    data class UpdateSync(val syncDuration: Sync.SyncDuration) : AppAction
    object AppLogout : AppAction
    data class DeleteLocalNotesOnLogout(val deleteOnLogout: Boolean) : AppAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class AppStateMachine(
    private val applicationContext: Context,
    private val userRepository: UserRepository,
    private val syncRepository: SyncRepository,
    private val noteMarkRepository: NoteMarkRepository
) : StateMachine<AppState, AppAction>(initialState = defaultAppState) {
    private var cachedUser: User? = null
    private var cachedSync: Sync? = null

    init {
        if (applicationContext is Activity) throw IllegalStateException("Context cannot be an Activity")

        spec {
            inState<AppState.NotLoggedIn> {
                condition(condition = { cachedUser != null }) {
                    onEnter { state ->
                        state.override { AppState.LoggedIn(user = cachedUser!!, sync = cachedSync) }
                    }
                }
                // All Flows while in the app state should be collected here
                collectWhileInState(flow = userRepository.getUser()) { user, state ->
                    user?.let { cachedUser = it }
                    cachedUser?.let { user ->
                        state.override {
                            AppState.LoggedIn(
                                connectionState = state.snapshot.connectionState,
                                user = user
                            )
                        }
                    } ?: state.noChange()
                }
                collectWhileInState(flow = applicationContext.observeConnectivityAsFlow()) { connected, state ->
                    state.mutate { state.snapshot.copy(connectionState = connected) }
                }
            }

            inState<AppState.LoggedIn> {
                condition(condition = { cachedSync != null } ) {
                    onEnterEffect { state ->
                        val sync = cachedSync ?: syncRepository.getSync().first()
                        val neverSynced = sync.lastUploadedTime == ""
                        val lastSyncTimeIsMoreThan5Minutes = getDifferenceFromTimestampInMinutes(isoOffsetDateTimeString = sync.lastUploadedTime) > 5L
                        // Start sync if never synced or last sync time is more than 5 mins and not syncing.
                        if (neverSynced || (lastSyncTimeIsMoreThan5Minutes && !sync.syncing)) {
                            applicationContext.cancelPreviousAndTriggerNewWork()
                        }
                    }
                    collectWhileInState(flow = userRepository.getUser()) { user, state ->
                        if (user == null) onLogoutSuccessful()
                        user?.let { state.noChange() } ?: state.override { AppState.NotLoggedIn(connectionState = state.snapshot.connectionState) }
                    }
                }
                collectWhileInState(flow = syncRepository.getSync()) { sync, state ->
                    cachedSync = sync
                    state.mutate { state.snapshot.copy(sync = sync) }
                }

                // All the actions valid for app state should be handled here
                on<AppAction.ConnectionStateConsumed> { _, state ->
                    state.mutate { state.snapshot.copy(connectionState = null) }
                }
                on<AppAction.UpdateSync> { action, state ->
                    val duration = when (action.syncDuration) {
                        Sync.SyncDuration.SYNC_DURATION_FIFTEEN_MINUTES -> Duration.ofMinutes(15)
                        Sync.SyncDuration.SYNC_DURATION_THIRTY_MINUTES -> Duration.ofMinutes(30)
                        Sync.SyncDuration.SYNC_DURATION_ONE_HOUR -> Duration.ofHours(1)
                        else -> Duration.ZERO
                    }

                    applicationContext.cancelPreviousAndTriggerNewWork(duration = duration)

                    val updatedSync = state.snapshot.sync?.toBuilder()?.setSyncDuration(action.syncDuration)?.build()
                    syncRepository.saveSyncDuration(syncDuration = action.syncDuration)
                    state.mutate { state.snapshot.copy(sync = updatedSync) }
                }
                onActionEffect<AppAction.DeleteLocalNotesOnLogout> { action, state ->
                    syncRepository.saveDeleteLocalNotesOnLogout(deleteLocalNotesOnLogout = action.deleteOnLogout)
                }
                on<AppAction.AppLogout> { _, state ->
                    noteMarkRepository.logout(
                        request = RefreshRequest(
                            refreshToken = userRepository.getUser().first()?.refreshToken ?: ""
                        )
                    ).getOrNull()?.let {
                        onLogoutSuccessful()
                        state.override {
                            AppState.NotLoggedIn(
                                connectionState = state.snapshot.connectionState
                            )
                        }
                    } ?: state.noChange()
                }
            }
        }
    }

    companion object {
        val defaultAppState = AppState.NotLoggedIn()
    }

    private suspend fun onLogoutSuccessful() {
        if (cachedSync?.deleteLocalNotesOnLogout == true) {
            noteMarkRepository.deleteAllLocalNotes()
        }
        cachedUser = null
        cachedSync = null
        userRepository.reset()
        syncRepository.reset()
    }
}

private fun Context.cancelPreviousAndTriggerNewWork(duration: Duration = Duration.ZERO) {
    val workManager = WorkManager.getInstance(context = this)

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .setRequiresStorageNotLow(true)
        .build()

    when (duration) {
        Duration.ZERO -> {
            workManager.cancelAllWorkByTag(tag = ONE_TIME_SYNC_WORK)
            workManager.enqueueUniqueWork(
                uniqueWorkName = "one_time_sync_work",
                existingWorkPolicy = ExistingWorkPolicy.REPLACE,
                request = OneTimeWorkRequestBuilder<NoteSyncWorker>()
                    .setConstraints(constraints)
                    .build()
            )
        }
        else -> {
            workManager.enqueueUniquePeriodicWork(
                uniqueWorkName = PERIODIC_SYNC_WORK,
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.REPLACE,
                request = PeriodicWorkRequestBuilder<NoteSyncWorker>(
                    repeatInterval = duration,
                    flexTimeInterval = Duration.ofMinutes(5)
                ).
                setConstraints(constraints).
                build())
        }
    }
}

private const val ONE_TIME_SYNC_WORK = "one_time_sync_work"
private const val PERIODIC_SYNC_WORK = "delayed_sync_work"

