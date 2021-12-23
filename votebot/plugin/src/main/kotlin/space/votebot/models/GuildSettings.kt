package space.votebot.models

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

@Serializable
data class GuildSettings(
    @SerialName("_id")
    @Contextual
    val id: Id<GuildSettings>,
    val guildId: Snowflake,
    val voteChannelId: Snowflake?
)
