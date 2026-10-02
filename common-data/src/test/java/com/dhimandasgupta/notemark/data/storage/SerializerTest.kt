package com.dhimandasgupta.notemark.data.storage

import com.dhimandasgupta.notemark.proto.Sync
import com.dhimandasgupta.notemark.proto.User
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SerializerTest {
  // A varint with its continuation bit set and nothing after it: a truncated message.
  private val corruptBytes = byteArrayOf(0xFF.toByte())

  @Test
  fun `user round-trips through the serializer`() = runTest {
    val serializer = UserSerializer()
    val user =
      User.newBuilder()
        .setUserName("name")
        .setAccessToken("access")
        .setRefreshToken("refresh")
        .build()

    val output = ByteArrayOutputStream()
    serializer.writeTo(user, output)

    assertEquals(user, serializer.readFrom(ByteArrayInputStream(output.toByteArray())))
  }

  @Test
  fun `corrupt user data falls back to the default value`() = runTest {
    val serializer = UserSerializer()

    assertEquals(serializer.defaultValue, serializer.readFrom(ByteArrayInputStream(corruptBytes)))
  }

  @Test
  fun `sync round-trips through the serializer`() = runTest {
    val serializer = SyncSerializer()
    val sync =
      Sync.newBuilder()
        .setSyncing(true)
        .setSyncDuration(Sync.SyncDuration.SYNC_DURATION_ONE_HOUR)
        .setLastDownloadedTime("downloaded")
        .setLastUploadedTime("uploaded")
        .setDeleteLocalNotesOnLogout(true)
        .build()

    val output = ByteArrayOutputStream()
    serializer.writeTo(sync, output)

    assertEquals(sync, serializer.readFrom(ByteArrayInputStream(output.toByteArray())))
  }

  @Test
  fun `corrupt sync data falls back to the default value`() = runTest {
    val serializer = SyncSerializer()

    assertEquals(serializer.defaultValue, serializer.readFrom(ByteArrayInputStream(corruptBytes)))
    assertEquals(Sync.SyncDuration.SYNC_DURATION_NONE, serializer.defaultValue.syncDuration)
  }
}
