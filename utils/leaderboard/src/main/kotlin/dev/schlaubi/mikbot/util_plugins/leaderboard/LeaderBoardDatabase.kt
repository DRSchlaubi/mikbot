package dev.schlaubi.mikbot.util_plugins.leaderboard

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.MemberBehavior
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection

object LeaderBoardDatabase : KordExKoinComponent {
    val leaderboardEntries = database.getCollection<LeaderBoardEntry>("leaderboards")
    val settings = database.getCollection<LeaderBoardSettings>("leaderboard_settings")
}

suspend fun CoroutineCollection<LeaderBoardEntry>.findByMember(member: MemberBehavior) = findOne(
    and(LeaderBoardEntry::guildId eq member.guildId, LeaderBoardEntry::userId eq member.id)
) ?: LeaderBoardEntry(newId(), member.id, member.guildId, 0, 0, Instant.DISTANT_PAST)

suspend fun CoroutineCollection<LeaderBoardEntry>.calculateRank(guild: Snowflake, points: Long) = countDocuments(
    and(LeaderBoardEntry::guildId eq guild, LeaderBoardEntry::points gt points)
)

suspend fun CoroutineCollection<LeaderBoardEntry>.countLeaderboardForGuild(guild: Snowflake) = countDocuments(
    LeaderBoardEntry::guildId eq guild
)

fun CoroutineCollection<LeaderBoardEntry>.leaderboardForGuild(guild: Snowflake) = find(
    LeaderBoardEntry::guildId eq guild
).descendingSort(LeaderBoardEntry::points).toFlow()

@Serializable
data class LeaderBoardSettings(
    @SerialName("_id")
    val guild: Snowflake,
    val levelUpChannel: Snowflake? = null,
    val levelUpMessage: String = "%mention%, you are now **Level %level%**"
)

@Serializable
data class LeaderBoardEntry(
    @SerialName("_id") @Contextual
    val id: Id<LeaderBoardEntry>,
    val userId: Snowflake,
    val guildId: Snowflake,
    val points: Long,
    val level: Int,
    val lastXpReceived: Instant
)
