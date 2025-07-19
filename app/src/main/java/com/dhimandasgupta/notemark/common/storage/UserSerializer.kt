package com.dhimandasgupta.notemark.common.storage

import androidx.datastore.core.Serializer
import com.dhimandasgupta.notemark.proto.User
import kotlinx.io.IOException
import java.io.InputStream
import java.io.OutputStream

class UserSerializer : Serializer<User> {
    override val defaultValue: User = User.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): User = try {
        User.parseFrom(input)
    } catch (_: IOException) {
        defaultValue
    }

    override suspend fun writeTo(
        t: User,
        output: OutputStream
    )  = t.writeTo(output)
}
