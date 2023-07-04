package dev.schlaubi.mikmusic.core.settings

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Serializable
data class GuildSettings(
    @SerialName("_id")
    val guildId: Snowflake,
    val djMode: Boolean = false,
    val djRole: Snowflake? = null,
    val announceSongs: Boolean = true,
    val musicChannelData: MusicChannelData? = null,
    val defaultSchedulerSettings: SchedulerSettings? = null,
    val useSponsorBlock: Boolean = true,
    @Contextual
    val leaveTimeout: Duration = 30.seconds
)

@Serializable
data class MusicChannelData(
    val musicChannel: Snowflake,
    val musicChannelMessage: Snowflake
)
