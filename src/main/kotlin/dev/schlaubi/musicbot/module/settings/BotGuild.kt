package dev.schlaubi.musicbot.module.settings

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmRecord
@Serializable
data class BotGuild(
    @SerialName("_id")
    val guildId: Snowflake,
    val djMode: Boolean = false,
    val djRole: Snowflake? = null,
    val announceSongs: Boolean = true,
    val musicChannelData: MusicChannelData? = null,
    val verified: Boolean = false
)

@JvmRecord
@Serializable
data class MusicChannelData(
    val musicChannel: Snowflake,
    val musicChannelMessage: Snowflake
)
