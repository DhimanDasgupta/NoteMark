package com.dhimandasgupta.notemark.data.local.datasource

import androidx.datastore.core.DataStore
import com.dhimandasgupta.notemark.app.di.SyncDataStore
import com.dhimandasgupta.notemark.proto.Sync
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

interface NoteSyncDataSource {
  fun getSync(): Flow<Sync>

  suspend fun saveSyncing(isSyncing: Boolean)

  suspend fun saveSyncDuration(syncDuration: Sync.SyncDuration)

  suspend fun saveLastDownloadedTime(downLoadedTime: String)

  suspend fun saveLastUploadedTime(uploadedTime: String)

  suspend fun saveDeleteLocalNotesOnLogout(deleteLocalNotesOnLogout: Boolean)

  suspend fun reset()
}

@Inject
class NoteSyncDataSourceImpl(@SyncDataStore private val syncDataStore: Lazy<DataStore<Sync>>) :
  NoteSyncDataSource {

  override fun getSync(): Flow<Sync> = syncDataStore.value.data

  override suspend fun saveSyncing(isSyncing: Boolean) {
    syncDataStore.value.updateData { transform ->
      transform.toBuilder().setSyncing(isSyncing).build()
    }
  }

  override suspend fun saveSyncDuration(syncDuration: Sync.SyncDuration) {
    syncDataStore.value.updateData { transform ->
      transform.toBuilder().setSyncDuration(syncDuration).build()
    }
  }

  override suspend fun saveLastDownloadedTime(downLoadedTime: String) {
    syncDataStore.value.updateData { transform ->
      transform.toBuilder().setLastDownloadedTime(downLoadedTime).build()
    }
  }

  override suspend fun saveLastUploadedTime(uploadedTime: String) {
    syncDataStore.value.updateData { transform ->
      transform.toBuilder().setLastUploadedTime(uploadedTime).build()
    }
  }

  override suspend fun saveDeleteLocalNotesOnLogout(deleteLocalNotesOnLogout: Boolean) {
    syncDataStore.value.updateData { transform ->
      transform.toBuilder().setDeleteLocalNotesOnLogout(deleteLocalNotesOnLogout).build()
    }
  }

  override suspend fun reset() {
    syncDataStore.value.updateData { transform ->
      transform.toBuilder().clear().build()
    }
  }
}
