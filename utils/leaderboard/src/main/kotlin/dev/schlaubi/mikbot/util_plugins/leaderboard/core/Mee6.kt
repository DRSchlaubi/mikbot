package dev.schlaubi.mikbot.util_plugins.leaderboard.core

import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.util_plugins.leaderboard.LeaderBoardDatabase
import dev.schlaubi.mikbot.util_plugins.leaderboard.LeaderBoardEntry
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.eq
import org.litote.kmongo.newId

private val client = HttpClient {
    install(JsonFeature) {
        val json = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
        }

        serializer = KotlinxSerializer(json)
    }
}

suspend fun importForGuild(guildId: Snowflake) {
    val items =
        client.get<Mee6LeaderBoard>("https://mee6.xyz/api/plugins/levels/leaderboard/$guildId?limit=999&page=0")

    LeaderBoardDatabase.leaderboardEntries.deleteMany(LeaderBoardEntry::guildId eq guildId)
    val entries = items.players.map {
        LeaderBoardEntry(
            newId(),
            it.id,
            it.guildId,
            it.xp.toLong(),
            it.level,
            Instant.DISTANT_PAST
        )
    }

    LeaderBoardDatabase.leaderboardEntries.insertMany(entries)
}

@Serializable
data class Mee6LeaderBoard(
    val admin: Boolean,
    @SerialName("banner_url")
    val bannerUrl: String?,
    @SerialName("is_member")
    val isMember: Boolean,
    val page: Int,
    val players: List<Mee6LeaderboardMember>
)

@Serializable
data class Mee6LeaderboardMember(
    val avatar: String,
    @SerialName("detailed_xp")
    val detailedXp: List<Int>,
    @SerialName("guild_id")
    val guildId: Snowflake,
    val id: Snowflake,
    val level: Int,
    @SerialName("message_count")
    val messageCount: Int,
    val xp: Int
)
