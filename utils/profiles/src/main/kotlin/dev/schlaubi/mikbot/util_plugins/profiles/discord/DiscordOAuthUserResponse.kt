package dev.schlaubi.mikbot.util_plugins.profiles.discord

import kotlinx.serialization.Serializable

@Serializable
data class DiscordOAuthUserResponse(
    val user: DiscordUserResponse
)
