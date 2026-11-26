package com.dhimandasgupta.notemark.data.local.datasource

import androidx.datastore.core.DataStore
import com.dhimandasgupta.notemark.core.di.SyncDataStore
import com.dhimandasgupta.notemark.data.storage.defaultSyncValue
import com.dhimandasgupta.notemark.proto.Sync
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class NoteSyncDataSourceImpl
internal constructor(@SyncDataStore syncDataStore: Lazy<DataStore<Sync>>) : NoteSyncDataSource {
  private val syncDataStore by syncDataStore

  override fun getSync(): Flow<Sync> = syncDataStore.data

  override suspend fun saveSyncing(isSyncing: Boolean) {
    syncDataStore.updateData { transform ->
      transform.toBuilder().setSyncing(isSyncing).build()
    }
  }

  override suspend fun saveSyncDuration(syncDuration: Sync.SyncDuration) {
    syncDataStore.updateData { transform ->
      transform.toBuilder().setSyncDuration(syncDuration).build()
    }
  }

  override suspend fun saveLastDownloadedTime(downLoadedTime: String) {
    syncDataStore.updateData { transform ->
      transform.toBuilder().setLastDownloadedTime(downLoadedTime).build()
    }
  }

  override suspend fun saveLastUploadedTime(uploadedTime: String) {
    syncDataStore.updateData { transform ->
      transform.toBuilder().setLastUploadedTime(uploadedTime).build()
    }
  }

  override suspend fun saveDeleteLocalNotesOnLogout(deleteLocalNotesOnLogout: Boolean) {
    syncDataStore.updateData { transform ->
      transform.toBuilder().setDeleteLocalNotesOnLogout(deleteLocalNotesOnLogout).build()
    }
  }

  override suspend fun reset() {
    syncDataStore.updateData { defaultSyncValue }
  }
}
