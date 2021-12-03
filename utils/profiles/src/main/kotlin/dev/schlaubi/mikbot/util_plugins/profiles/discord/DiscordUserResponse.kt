package dev.schlaubi.mikbot.util_plugins.profiles.discord

import kotlinx.serialization.Serializable

@Serializable
data class DiscordUserResponse(
    val id: String,
    val username: String,
    val discriminator: String
)
