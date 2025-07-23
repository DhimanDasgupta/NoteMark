package com.dhimandasgupta.notemark.common.storage

import androidx.datastore.core.Serializer
import com.dhimandasgupta.notemark.proto.Sync
import kotlinx.io.IOException
import java.io.InputStream
import java.io.OutputStream

class SyncSerializer : Serializer<Sync> {
    override val defaultValue: Sync = defaultSyncValue

    override suspend fun readFrom(input: InputStream): Sync  = try {
        Sync.parseFrom(input)
    } catch (_: IOException) {
        defaultValue
    }

    override suspend fun writeTo(
        t: Sync,
        output: OutputStream
    ) {
        t.writeTo(output)
    }
}

private val defaultSyncValue = Sync.newBuilder().apply {
    syncing = false
    lastUploadedTime = "0"
    lastDownloadedTime = "0"
    syncDuration = Sync.SyncDuration.SYNC_DURATION_NONE
    deleteLocalNotesOnLogout = false
}.build()