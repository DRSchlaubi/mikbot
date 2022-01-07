package space.votebot.transformer

import dev.kord.common.entity.Snowflake
import dev.kord.common.exception.RequestException
import dev.kord.core.Kord
import dev.kord.core.entity.channel.GuildChannel
import mu.KotlinLogging

object ChannelMessageTransformer : MessageTransformer {

    private val regex = Regex("<#(\\d+)>")

    override suspend fun transform(message: String, kord: Kord, inMarkdownContext: Boolean): String {
        if (inMarkdownContext) {
            return message
        }
        return regex.findAll(message).toSet().mapNotNull { match ->
            val (channelId) = match.destructured
            val channel = kord.tryGetChannel(channelId) ?: return@mapNotNull null
            match.value to channel.name
        }.fold(message) { acc, (value, name) ->
            acc.replace(value, "#$name")
        }
    }
}

private val logger = KotlinLogging.logger {}

private suspend inline fun Kord.tryGetChannel(id: String): GuildChannel? {
    return try {
        getChannelOf(Snowflake(id))
    } catch (e: NumberFormatException) {
        null
    } catch (e: RequestException) {
        logger.error(e) { "An error occurred while trying to get the channel ${id}. This shouldn't happen!" }
        null
    }
}
