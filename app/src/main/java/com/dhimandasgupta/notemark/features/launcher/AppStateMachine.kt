package com.dhimandasgupta.notemark.features.launcher

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dhimandasgupta.notemark.app.NoteSyncWorker
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
) : StateMachine<AppState, AppAction>(defaultAppState) {
    private var cachedUser: User? = null
    private var cachedSync: Sync? = null

    init {
        spec {
            inState<AppState.NotLoggedIn> {
                condition({ cachedUser != null }) {
                    onEnter { state ->
                        state.override { AppState.LoggedIn(user = cachedUser!!, sync = cachedSync) }
                    }
                }
                // All Flows while in the app state should be collected here
                collectWhileInState(userRepository.getUser()) { user, state ->
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
                collectWhileInState(applicationContext.observeConnectivityAsFlow()) { connected, state ->
                    state.mutate { state.snapshot.copy(connectionState = connected) }
                }
            }

            inState<AppState.LoggedIn> {
                condition( { cachedSync != null } ) {
                    onEnterEffect { state ->
                        val sync = cachedSync ?: syncRepository.getSync().first()
                        if (getDifferenceFromTimestampInMinutes(sync.lastUploadedTime) > 5L && !sync.syncing) {
                            applicationContext.cancelPreviousAndTriggerNewWork()
                        }
                    }
                }
                collectWhileInState(syncRepository.getSync()) { sync, state ->
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
                    syncRepository.saveSyncDuration(action.syncDuration)
                    state.mutate { state.snapshot.copy(sync = updatedSync) }
                }
                onActionEffect<AppAction.DeleteLocalNotesOnLogout> { action, state ->
                    syncRepository.saveDeleteLocalNotesOnLogout(action.deleteOnLogout)
                }
                on<AppAction.AppLogout> { _, state ->
                    noteMarkRepository.logout(
                        request = RefreshRequest(
                            refreshToken = userRepository.getUser().first()?.refreshToken ?: ""
                        )
                    ).getOrNull()?.let {
                        if (cachedSync?.deleteLocalNotesOnLogout == true) {
                            noteMarkRepository.deleteAllLocalNotes()
                        }
                        cachedUser = null
                        cachedSync = null
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
}

private fun Context.cancelPreviousAndTriggerNewWork(duration: Duration = Duration.ZERO) {
    val workManager = WorkManager.getInstance(this)
    workManager.cancelAllWork()

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .setRequiresStorageNotLow(true)
        .build()

    when (duration) {
        Duration.ZERO -> {
            workManager.enqueue(
                request = OneTimeWorkRequestBuilder<NoteSyncWorker>()
                    .setConstraints(constraints)
                    .build()
            )
        }
        else -> {
            workManager.enqueue(
                request = PeriodicWorkRequestBuilder<NoteSyncWorker>(
                    repeatInterval = duration
                ).
                setConstraints(constraints).
                build()
            )
        }
    }
}

