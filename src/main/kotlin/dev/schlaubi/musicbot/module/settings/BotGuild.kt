package dev.schlaubi.musicbot.module.settings

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@JvmRecord
@Serializable
data class BotGuild(
    @SerialName("_id")
    val guildId: Snowflake,
    val djMode: Boolean = false,
    val djRole: Snowflake? = null,
    val announceSongs: Boolean = true,
    val musicChannelData: MusicChannelData? = null,
    val verified: Boolean = false,
    val defaultSchedulerSettings: SchedulerSettings? = null,
    val useSponsorBlock: Boolean = true,
    @Contextual
    val leaveTimeout: Duration = Duration.seconds(30)
)

@JvmRecord
@Serializable
data class MusicChannelData(
    val musicChannel: Snowflake,
    val musicChannelMessage: Snowflake
)
