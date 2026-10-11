package com.dhimandasgupta.notemark.features.notelist

import com.dhimandasgupta.notemark.common.android.AppShortcuts
import com.dhimandasgupta.notemark.common.android.SyncScheduler
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.data.repository.NoteMarkRepository
import com.dhimandasgupta.notemark.data.repository.SyncRepository
import com.dhimandasgupta.notemark.data.repository.UserRepository
import com.dhimandasgupta.notemark.proto.Sync
import com.dhimandasgupta.notemark.proto.User
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.mockito.kotlin.mock

class FakeUserRepository(user: User? = null) : UserRepository {
  val user = MutableStateFlow(value = user)
  var resetCount = 0
    private set

  override fun getUser(): Flow<User?> = user

  override suspend fun saveUser(user: User) {
    this.user.value = user
  }

  override suspend fun saveBearToken(token: BearerTokens) = Unit

  override suspend fun deleteUser() {
    user.value = null
  }

  override suspend fun reset() {
    resetCount++
    user.value = null
  }
}

class FakeSyncRepository(sync: Sync = Sync.getDefaultInstance()) : SyncRepository {
  val sync = MutableStateFlow(value = sync)
  var resetCount = 0
    private set

  override fun getSync(): Flow<Sync> = sync

  override suspend fun saveSyncing(isSyncing: Boolean) = edit { setSyncing(isSyncing) }

  override suspend fun saveSyncDuration(syncDuration: Sync.SyncDuration) = edit {
    setSyncDuration(syncDuration)
  }

  override suspend fun saveLastDownloadedTime(downLoadedTime: String) = edit {
    setLastDownloadedTime(downLoadedTime)
  }

  override suspend fun saveLastUploadedTime(uploadedTime: String) = edit {
    setLastUploadedTime(uploadedTime)
  }

  override suspend fun saveDeleteLocalNotesOnLogout(deleteLocalNotesOnLogout: Boolean) = edit {
    setDeleteLocalNotesOnLogout(deleteLocalNotesOnLogout)
  }

  override suspend fun reset() {
    resetCount++
    sync.value = Sync.getDefaultInstance()
  }

  private fun edit(block: Sync.Builder.() -> Unit) = sync.update { current ->
    current.toBuilder().apply(block).build()
  }
}

/** Only logout and local note deletion are used by the app state machine. */
class FakeNoteMarkRepository(private val logoutResult: Result<Unit>) :
  NoteMarkRepository by mock<NoteMarkRepository>() {
  val logoutRequests = mutableListOf<RefreshRequest>()
  var deleteAllLocalNotesCount = 0
    private set

  override suspend fun logout(request: RefreshRequest): Result<Unit> {
    logoutRequests += request
    return logoutResult
  }

  override suspend fun deleteAllLocalNotes(): Boolean {
    deleteAllLocalNotesCount++
    return true
  }
}

class FakeSyncScheduler : SyncScheduler {
  val isSyncing = MutableStateFlow(value = false)
  val periodicIntervals = mutableListOf<Duration>()
  var oneTimeSyncCount = 0
    private set

  var cancelPeriodicSyncCount = 0
    private set

  var cancelSyncWorkCount = 0
    private set

  override fun triggerOneTimeSync() {
    oneTimeSyncCount++
  }

  override fun schedulePeriodicSync(interval: Duration) {
    periodicIntervals += interval
  }

  override fun cancelPeriodicSync() {
    cancelPeriodicSyncCount++
  }

  override fun observeSyncRunning(): Flow<Boolean> = isSyncing

  override suspend fun cancelSyncWork() {
    cancelSyncWorkCount++
  }
}

class FakeAppShortcuts : AppShortcuts {
  var addCount = 0
    private set

  var removeCount = 0
    private set

  override fun addCreateNewNoteShortcut() {
    addCount++
  }

  override fun removeCreateNewNoteShortcut() {
    removeCount++
  }
}
