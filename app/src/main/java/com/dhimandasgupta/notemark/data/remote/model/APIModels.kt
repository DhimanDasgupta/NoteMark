package com.dhimandasgupta.notemark.data.remote.model

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class RegisterRequest(
    @SerialName("username")val username: String,
    @SerialName("email")val email: String,
    @SerialName("password")val password: String
)

@Keep
@Serializable
data class LoginRequest(
    @SerialName("email")val email: String,
    @SerialName("password")val password: String
)

@Keep
@Serializable
data class AuthResponse(
    @SerialName("accessToken")val accessToken: String,
    @SerialName("refreshToken")val refreshToken: String,
    @SerialName("username")val username: String
)

@Keep
@Serializable
data class RefreshRequest(
    @SerialName("refreshToken")val refreshToken: String
)

@Keep
@Serializable
data class RefreshResponse(
    @SerialName("accessToken")val accessToken: String,
    @SerialName("refreshToken")val refreshToken: String
)

@Keep
@Serializable
data class NoteResponse(
    @SerialName("notes")val notes: List<Note>,
    @SerialName("total")val total: Int
)

@Keep
@Serializable
data class Note(
    @SerialName("id")val uuid: String,
    @SerialName("title")val title: String,
    @SerialName("content")val content: String,
    @SerialName("createdAt")val createdAt: String,
    @SerialName("lastEditedAt")val lastEditedAt: String,
)