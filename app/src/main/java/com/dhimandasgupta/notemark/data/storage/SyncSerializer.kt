package com.dhimandasgupta.notemark.data.storage

import androidx.datastore.core.Serializer
import com.dhimandasgupta.notemark.proto.Sync
import java.io.InputStream
import java.io.OutputStream
import kotlinx.io.IOException

class SyncSerializer : Serializer<Sync> {
  override val defaultValue: Sync = defaultSyncValue

  override suspend fun readFrom(input: InputStream): Sync =
    try {
      Sync.parseFrom(input)
    } catch (_: IOException) {
      defaultValue
    }

  override suspend fun writeTo(
    t: Sync,
    output: OutputStream,
  ) {
    t.writeTo(output)
  }
}

internal val defaultSyncValue: Sync =
  Sync.newBuilder()
    .apply {
      syncing = false
      lastUploadedTime = ""
      lastDownloadedTime = ""
      syncDuration = Sync.SyncDuration.SYNC_DURATION_NONE
      deleteLocalNotesOnLogout = false
    }
    .build()
