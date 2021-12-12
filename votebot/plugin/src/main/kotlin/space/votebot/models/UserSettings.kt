package space.votebot.models

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import space.votebot.common.models.StoredPollSettings

@Serializable
data class UserSettings(
    val userId: Snowflake,
    val settings: StoredPollSettings
)
