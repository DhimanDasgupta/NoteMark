package com.dhimandasgupta.notemark.data

import com.dhimandasgupta.notemark.data.local.datasource.NoteSyncDataSource
import com.dhimandasgupta.notemark.proto.Sync
import dev.zacsweers.metro.Inject
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

@Inject
class SyncRepositoryImpl(private val noteSyncDataSource: Lazy<NoteSyncDataSource>) :
  SyncRepository {
  override fun getSync(): Flow<Sync> = noteSyncDataSource.value.getSync()

  override suspend fun saveSyncing(isSyncing: Boolean) =
    noteSyncDataSource.value.saveSyncing(isSyncing = isSyncing)

  override suspend fun saveSyncDuration(syncDuration: Sync.SyncDuration) =
    noteSyncDataSource.value.saveSyncDuration(syncDuration = syncDuration)

  override suspend fun saveLastDownloadedTime(downLoadedTime: String) =
    noteSyncDataSource.value.saveLastDownloadedTime(downLoadedTime = downLoadedTime)

  override suspend fun saveLastUploadedTime(uploadedTime: String) =
    noteSyncDataSource.value.saveLastUploadedTime(uploadedTime = uploadedTime)

  override suspend fun saveDeleteLocalNotesOnLogout(deleteLocalNotesOnLogout: Boolean) =
    noteSyncDataSource.value.saveDeleteLocalNotesOnLogout(
      deleteLocalNotesOnLogout = deleteLocalNotesOnLogout
    )

  override suspend fun reset() = noteSyncDataSource.value.reset()
}
