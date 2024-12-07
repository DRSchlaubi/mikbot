package dev.schlaubi.mikmusic.core.settings

import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikmusic.api.types.SchedulerSettings
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    @SerialName("_id")
    val id: Snowflake,
    val defaultSchedulerSettings: SchedulerSettings? = null
)
