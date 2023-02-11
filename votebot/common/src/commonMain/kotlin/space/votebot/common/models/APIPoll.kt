package space.votebot.common.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
public data class APIPoll(
    val id: String,
    val guildId: String,
    val author: DiscordUser?,
    val title: String,
    val options: List<VoteOption>,
    val createdAt: Instant,
    val settings: FinalPollSettings
)

@Serializable
public data class PartialAPIPoll(
    val id: String,
    val guildId: String,
    val author: DiscordUser?,
    val voteCount: Int,
    val title: String
)
