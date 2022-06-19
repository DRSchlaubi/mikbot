package space.votebot.core

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import org.bson.Document
import org.litote.kmongo.coroutine.CoroutineCollection
import space.votebot.common.models.Poll
import space.votebot.models.GuildSettings
import space.votebot.models.UserSettings
import space.votebot.util.toPollMessage

object VoteBotDatabase : KordExKoinComponent {
    val polls = database.getCollection<Poll>("polls")
    val userSettings = database
        .getCollection<UserSettings>("user_settings")
    val guildSettings = database
        .getCollection<GuildSettings>("guild_settings")
}

// For some reason KMongo cannot seem to serialize this properly
suspend fun CoroutineCollection<Poll>.findOneByMessage(pollMessage: Poll.Message) =
    findOne(
        Document.parse(
            """{"messages": {"messageId": NumberLong("${pollMessage.messageId}"), 
        |"channelId": NumberLong("${pollMessage.channelId}"),
        |"guildId": NumberLong("${pollMessage.guildId}")}}
        |""".trimMargin()
        )
    )

suspend fun CoroutineCollection<Poll>.findOneByMessage(message: Message) = findOneByMessage(message.toPollMessage())

suspend fun CoroutineCollection<GuildSettings>.findOneByGuild(guildId: Snowflake) =
    findOne(
        Document.parse(
            """{"guildId": NumberLong("$guildId")}""".trimIndent()
        )
    )
