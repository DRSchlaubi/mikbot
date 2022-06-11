package dev.schlaubi.mikbot.util_plugins.leaderboard.api

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Member
import dev.kord.rest.Image
import dev.schlaubi.mikbot.plugin.api.util.effectiveAvatar
import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import dev.schlaubi.mikbot.util_plugins.leaderboard.LeaderBoardDatabase
import dev.schlaubi.mikbot.util_plugins.leaderboard.LeaderBoardEntry
import dev.schlaubi.mikbot.util_plugins.leaderboard.leaderboardForGuild
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.litote.kmongo.eq
import org.pf4j.Extension

@Serializable
@Resource("/leaderboard")
class LeaderBoard {

    @Serializable
    @Resource("guilds/{guildId}")
    data class ForGuild(val guildId: Long, val parent: LeaderBoard)

    @Serializable
    @Resource("users/{userId}")
    data class ForUser(val userId: Long, val parent: LeaderBoard)

}

@Extension
class KtorServer : KtorExtensionPoint, KoinComponent {
    val kord by inject<Kord>()

    override fun Application.apply() {
        routing {
            get<LeaderBoard.ForGuild> { (id) ->
                val snowflake = Snowflake(id)
                val guild =
                    runCatching { kord.getGuild(snowflake) }.getOrNull() ?: return@get context.respond(HttpStatusCode.NotFound)
                val members = guild.members.toList().associateBy(Member::id)
                val leaderboard = LeaderBoardDatabase.leaderboardEntries.leaderboardForGuild(snowflake).toList()

                context.respond(
                    LeaderBoardEntity(
                        guild.name,
                        guild.getIconUrl(Image.Format.WEBP),
                        leaderboard.map {
                            val member = members[it.userId]
                            leaderBoardMember(member, it)
                        }
                    )
                )
            }

            get<LeaderBoard.ForUser> { (userId) ->
                val entries = LeaderBoardDatabase.leaderboardEntries.find(
                    LeaderBoardEntry::userId eq Snowflake(userId)
                ).toList()

                context.respond(MultiGuildMember(
                    entries.associate {
                        val member = kord.getGuild(it.guildId)?.getMemberOrNull(it.userId)

                        it.guildId.toString() to leaderBoardMember(member, it)
                    }
                ))

            }
        }
    }

    private fun leaderBoardMember(
        member: Member?,
        entry: LeaderBoardEntry,
    ): LeaderBoardMember {
        return LeaderBoardMember(
            entry.userId,
            member?.username,
            member?.nickname ?: member?.username,
            member?.discriminator,
            entry.points,
            entry.level,
            entry.lastXpReceived,
            member?.memberAvatar?.url ?: member?.effectiveAvatar
        )
    }
}

@Serializable
data class LeaderBoardEntity(
    @SerialName("guild_name")
    val guildName: String,
    @SerialName("guild_icon")
    val guildIcon: String?,
    val members: List<LeaderBoardMember>,
)

@Serializable
data class MultiGuildMember(
    val data: Map<String, LeaderBoardMember>,
)

@Serializable
data class LeaderBoardMember(
    @SerialName("user_id")
    val userId: Snowflake,
    val username: String?,
    @SerialName("effective_name")
    val effectiveName: String?,
    val discriminator: String?,
    val points: Long,
    val level: Int,
    @SerialName("last_xp_received")
    val lastXpReceived: Instant,
    @SerialName("avatar_url")
    val avatarUrl: String?,
)
