package dev.schlaubi.mikmusic.api.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OAuth2AccessTokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Long,
    @SerialName("refresh_token")
    val refreshToken: String,
)

@Serializable
data class OAuth2TokenRequest(
    val code: String? = null,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("grant_type")
    val grantType: String,
    val state: String? = null,
)
