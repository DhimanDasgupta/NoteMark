package com.dhimandasgupta.notemark.data.local.datasource

import androidx.datastore.core.DataStore
import com.dhimandasgupta.notemark.proto.Sync
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

class NoteSyncDataSourceImpl(
    private val syncDataStore: DataStore<Sync>
) : NoteSyncDataSource {

    override fun getSync(): Flow<Sync> = syncDataStore.data

    override suspend fun saveSyncing(isSyncing: Boolean) {
        syncDataStore.updateData { transform ->
            transform.toBuilder()
                .setSyncing(isSyncing)
                .build()
        }
    }

    override suspend fun saveSyncDuration(syncDuration: Sync.SyncDuration) {
        syncDataStore.updateData { transform ->
            transform.toBuilder()
                .setSyncDuration(syncDuration)
                .build()
        }
    }

    override suspend fun saveLastDownloadedTime(downLoadedTime: String) {
        syncDataStore.updateData { transform ->
            transform.toBuilder()
                .setLastDownloadedTime(downLoadedTime)
                .build()
        }
    }

    override suspend fun saveLastUploadedTime(uploadedTime: String) {
        syncDataStore.updateData { transform ->
            transform.toBuilder()
                .setLastUploadedTime(uploadedTime)
                .build()
        }
    }

    override suspend fun saveDeleteLocalNotesOnLogout(deleteLocalNotesOnLogout: Boolean) {
        syncDataStore.updateData { transform ->
            transform.toBuilder()
                .setDeleteLocalNotesOnLogout(deleteLocalNotesOnLogout)
                .build()
        }

    }

    override suspend fun reset() {
        syncDataStore.updateData { transform ->
            transform.toBuilder().clear().build()
        }
    }
}