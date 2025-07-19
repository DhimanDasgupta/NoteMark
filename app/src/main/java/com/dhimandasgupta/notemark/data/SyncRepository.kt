package com.dhimandasgupta.notemark.data

import com.dhimandasgupta.notemark.data.local.datasource.NoteSyncDataSource
import com.dhimandasgupta.notemark.proto.Sync
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    fun getSync(): Flow<Sync>
    suspend fun saveSyncing(isSyncing: Boolean)
    suspend fun saveSyncDuration(syncDuration: Sync.SyncDuration)
    suspend fun saveLastDownloadedTime(downLoadedTime: Long)
    suspend fun saveLastUploadedTime(uploadedTime: Long)
}

class SyncRepositoryImpl(
    private val noteSyncDataSource: NoteSyncDataSource
) : SyncRepository {
    override fun getSync(): Flow<Sync> = noteSyncDataSource.getSync()
    override suspend fun saveSyncing(isSyncing: Boolean) = noteSyncDataSource.saveSyncing(isSyncing)
    override suspend fun saveSyncDuration(syncDuration: Sync.SyncDuration) =
        noteSyncDataSource.saveSyncDuration(syncDuration)
    override suspend fun saveLastDownloadedTime(downLoadedTime: Long) = noteSyncDataSource.saveLastDownloadedTime(downLoadedTime)
    override suspend fun saveLastUploadedTime(uploadedTime: Long) = noteSyncDataSource.saveLastUploadedTime(uploadedTime)
}