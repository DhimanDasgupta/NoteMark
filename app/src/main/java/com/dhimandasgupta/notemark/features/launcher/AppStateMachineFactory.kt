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
import com.dhimandasgupta.notemark.common.extensions.getAppVersionName
import com.dhimandasgupta.notemark.common.getDifferenceFromTimestampInMinutes
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.SyncRepository
import com.dhimandasgupta.notemark.data.UserRepository
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.proto.Sync
import com.dhimandasgupta.notemark.proto.User
import com.dhimandasgupta.notemark.proto.sync
import com.freeletics.flowredux2.initializeWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import java.time.Duration
import com.freeletics.flowredux2.FlowReduxStateMachineFactory as StateMachineFactory

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
        val appVersionName: String
    ) : AppState
}

sealed interface AppAction {
    data class UpdateSync(val syncDuration: Sync.SyncDuration) : AppAction
    object AppLogout : AppAction
    data class DeleteLocalNotesOnLogout(val deleteOnLogout: Boolean) : AppAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class AppStateMachineFactory(
    private val applicationContext: Context,
    private val userRepository: UserRepository,
    private val syncRepository: SyncRepository,
    private val noteMarkRepository: NoteMarkRepository
) : StateMachineFactory<AppState, AppAction>() {
    init {
        if (applicationContext is Activity) throw IllegalStateException("Context cannot be an Activity")

        spec {
            initializeWith { defaultAppState }

            inState<AppState.NotLoggedIn> {
                collectWhileInState(flow = userRepository.getUser().distinctUntilChanged()) { user ->
                    user?.let {
                        override {
                            AppState.LoggedIn(
                                connectionState = connectionState,
                                user = user,
                                appVersionName = applicationContext.getAppVersionName()
                            )
                        }
                    } ?: noChange()
                }
                collectWhileInState(flow = applicationContext.observeConnectivityAsFlow().distinctUntilChanged()) { connected ->
                    mutate { copy(connectionState = connected) }
                }
            }

            inState<AppState.LoggedIn> {
                onEnterEffect { syncOnEnter() }
                collectWhileInState(flow = applicationContext.observeConnectivityAsFlow().distinctUntilChanged()) { connected ->
                    mutate { copy(connectionState = connected) }
                }
                collectWhileInState(flow = syncRepository.getSync()) { sync ->
                    mutate { copy(sync = sync) }
                }
                collectWhileInState(flow = userRepository.getUser().distinctUntilChanged()) { user ->
                    if (user == null) {
                        override {
                            AppState.NotLoggedIn(
                                connectionState = connectionState
                            )
                        }
                    } else {
                        noChange()
                    }
                }

                // All the actions valid for app state should be handled here
                on<AppAction.UpdateSync> { action ->
                    val duration = when (action.syncDuration) {
                        Sync.SyncDuration.SYNC_DURATION_FIFTEEN_MINUTES -> Duration.ofMinutes(15)
                        Sync.SyncDuration.SYNC_DURATION_THIRTY_MINUTES -> Duration.ofMinutes(30)
                        Sync.SyncDuration.SYNC_DURATION_ONE_HOUR -> Duration.ofHours(1)
                        else -> Duration.ZERO
                    }

                    applicationContext.cancelPreviousAndTriggerNewWork(duration = duration)

                    val updatedSync = sync{}.toBuilder()?.setSyncDuration(action.syncDuration)?.build()
                    syncRepository.saveSyncDuration(syncDuration = action.syncDuration)
                    mutate { copy(sync = updatedSync) }
                }
                onActionEffect<AppAction.DeleteLocalNotesOnLogout> { action ->
                    syncRepository.saveDeleteLocalNotesOnLogout(deleteLocalNotesOnLogout = action.deleteOnLogout)
                }
                on<AppAction.AppLogout> { _ ->
                    if (snapshot.connectionState == ConnectionState.Unavailable) return@on noChange()

                    noteMarkRepository.logout(
                        request = RefreshRequest(
                            refreshToken = userRepository.getUser().first()?.refreshToken ?: ""
                        )
                    ).getOrNull()?.let {
                        onLogoutSuccessful(deleteLocalNotesOnLogout = syncRepository.getSync().first().deleteLocalNotesOnLogout)
                        override {
                            AppState.NotLoggedIn(
                                connectionState = connectionState
                            )
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
        val neverSynced = sync.lastUploadedTime == ""
        val lastSyncTimeIsMoreThan5Minutes = getDifferenceFromTimestampInMinutes(isoOffsetDateTimeString = sync.lastUploadedTime) > 5L
        // Start sync if never synced or last sync time is more than 5 mins and not syncing.
        if (neverSynced || (lastSyncTimeIsMoreThan5Minutes && !sync.syncing)) {
            applicationContext.cancelPreviousAndTriggerNewWork()
        }
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
                uniqueWorkName = ONE_TIME_SYNC_WORK,
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
                    flexTimeInterval = Duration.ofMinutes(5) // 5 mins earlier or after the schedule
                ).
                setConstraints(constraints).
                build())
        }
    }
}

private const val ONE_TIME_SYNC_WORK = "one_time_sync_work"
private const val PERIODIC_SYNC_WORK = "delayed_sync_work"

