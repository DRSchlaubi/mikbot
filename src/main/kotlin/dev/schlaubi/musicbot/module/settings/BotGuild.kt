package dev.schlaubi.musicbot.module.settings

import kotlinx.serialization.Serializable

@JvmRecord
@Serializable
data class BotGuild(
    val djMode: Boolean = false,
    val announceSongs: Boolean = true
)
