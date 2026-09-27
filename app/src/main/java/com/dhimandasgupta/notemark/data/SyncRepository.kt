package com.dhimandasgupta.notemark.data

import com.dhimandasgupta.notemark.proto.Sync
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
  fun getSync(): Flow<Sync>

  suspend fun saveSyncing(isSyncing: Boolean)

  suspend fun saveSyncDuration(syncDuration: Sync.SyncDuration)

  suspend fun saveLastDownloadedTime(downLoadedTime: String)

  suspend fun saveLastUploadedTime(uploadedTime: String)

  suspend fun saveDeleteLocalNotesOnLogout(deleteLocalNotesOnLogout: Boolean)

  suspend fun reset()
}
