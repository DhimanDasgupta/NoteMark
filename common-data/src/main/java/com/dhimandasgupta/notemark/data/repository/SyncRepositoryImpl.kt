package com.dhimandasgupta.notemark.data.repository

import com.dhimandasgupta.notemark.data.local.datasource.NoteSyncDataSource
import com.dhimandasgupta.notemark.proto.Sync
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class SyncRepositoryImpl internal constructor(noteSyncDataSource: Lazy<NoteSyncDataSource>) :
  SyncRepository {
  private val noteSyncDataSource by noteSyncDataSource

  override fun getSync(): Flow<Sync> = noteSyncDataSource.getSync()

  override suspend fun saveSyncing(isSyncing: Boolean) =
    noteSyncDataSource.saveSyncing(isSyncing = isSyncing)

  override suspend fun saveSyncDuration(syncDuration: Sync.SyncDuration) =
    noteSyncDataSource.saveSyncDuration(syncDuration = syncDuration)

  override suspend fun saveLastDownloadedTime(downLoadedTime: String) =
    noteSyncDataSource.saveLastDownloadedTime(downLoadedTime = downLoadedTime)

  override suspend fun saveLastUploadedTime(uploadedTime: String) =
    noteSyncDataSource.saveLastUploadedTime(uploadedTime = uploadedTime)

  override suspend fun saveDeleteLocalNotesOnLogout(deleteLocalNotesOnLogout: Boolean) =
    noteSyncDataSource.saveDeleteLocalNotesOnLogout(
      deleteLocalNotesOnLogout = deleteLocalNotesOnLogout
    )

  override suspend fun reset() = noteSyncDataSource.reset()
}
