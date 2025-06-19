package com.dhimandasgupta.notemark.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @SerialName("username")
    val userName: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    @SerialName("username")
    val userName: String
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)