package space.votebot.core

import dev.kord.core.entity.Message
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import org.bson.Document
import org.koin.core.component.KoinComponent
import org.litote.kmongo.coroutine.CoroutineCollection
import space.votebot.common.models.Poll
import space.votebot.models.UserSettings
import space.votebot.util.toPollMessage

object VoteBotDatabase : KoinComponent {
    val polls = database.getCollection<Poll>("polls")
    val userSettings = database
        .getCollection<UserSettings>("user_settings")
}

// For some reason KMongo cannot seem to serialize this properly
suspend fun CoroutineCollection<Poll>.findByMessage(pollMessage: Poll.Message) =
    findOne(
        Document.parse(
            """{"messages": {"messageId": NumberLong("${pollMessage.messageId}"), 
        |"channelId": NumberLong("${pollMessage.channelId}"),
        |"guildId": NumberLong("${pollMessage.guildId}")}}
        |""".trimMargin()
        )
    )

suspend fun CoroutineCollection<Poll>.findByMessage(message: Message) = findByMessage(message.toPollMessage())
