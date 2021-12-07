package dev.schlaubi.mikbot.util_plugins.profiles.social

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TwitchUser(
    @SerialName("client_id") val clientId: String,
    @SerialName("login") val login: String,
    @SerialName("user_id") val userId: String,
) : User {
    override val displayName: String
        get() = login
    override val id: String
        get() = userId
    override val url: String
        get() = "https://twitch.tv/$login"
}
