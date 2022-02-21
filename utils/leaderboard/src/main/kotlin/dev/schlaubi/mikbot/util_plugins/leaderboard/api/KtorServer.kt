package dev.schlaubi.mikbot.util_plugins.leaderboard.api

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.rest.Image
import dev.schlaubi.mikbot.plugin.api.util.effectiveAvatar
import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import dev.schlaubi.mikbot.util_plugins.leaderboard.LeaderBoardDatabase
import dev.schlaubi.mikbot.util_plugins.leaderboard.leaderboardForGuild
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.pf4j.Extension

@Location("/leaderboard/{guildId}")
data class LeaderBoard(val guildId: Long)

@Extension
class KtorServer : KtorExtensionPoint, KoinComponent {
    val kord by inject<Kord>()

    override fun Application.apply() {
        routing {
            get<LeaderBoard> { (guildId) ->
                val id = Snowflake(guildId)
                val guild =
                    runCatching { kord.getGuild(id) }.getOrNull() ?: return@get context.respond(HttpStatusCode.NotFound)
                val members = guild.members.toList().associateBy { it.id }
                val leaderboard = LeaderBoardDatabase.leaderboardEntries.leaderboardForGuild(id).toList()

                context.respond(
                    LeaderBoardEntity(
                        guild.name,
                        guild.getIconUrl(Image.Format.WEBP),
                        leaderboard.map {
                            LeaderBoardMember(
                                it.userId,
                                it.points,
                                it.level,
                                it.lastXpReceived,
                                members[it.userId]!!.let { member ->
                                    member.memberAvatar?.url ?: member.effectiveAvatar
                                }
                            )
                        }
                    )
                )
            }
        }
    }
}

@Serializable
data class LeaderBoardEntity(
    @SerialName("guild_name")
    val guildName: String,
    @SerialName("guild_icon")
    val guildIcon: String?,
    val members: List<LeaderBoardMember>
)

@Serializable
data class LeaderBoardMember(
    @SerialName("guild_id")
    val userId: Snowflake,
    val points: Long,
    val level: Int,
    @SerialName("last_xp_received")
    val lastXpReceived: Instant,
    @SerialName("avatar_url")
    val avatarUrl: String
)
