package space.votebot.transformer

import dev.kord.common.entity.Snowflake
import dev.kord.common.exception.RequestException
import dev.kord.core.Kord
import dev.kord.core.entity.channel.GuildChannel
import mu.KotlinLogging

object ChannelMessageTransformer : RegexReplaceTransformer() {
    override val regex = Regex("<#(\\d+)>")
    override val skipInMarkdownContext: Boolean = true

    override suspend fun TransformerContext.transform(match: MatchResult): String? {
        val (channelId) = match.destructured
        val channel = kord.tryGetChannel(channelId) ?: return null
        return "#${channel.name}"
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
