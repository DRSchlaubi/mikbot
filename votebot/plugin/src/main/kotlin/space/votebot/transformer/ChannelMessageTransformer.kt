package space.votebot.transformer

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.GuildChannel

object ChannelMessageTransformer : RegexReplaceTransformer() {
    override val regex = Regex("<#(\\d+)>")
    override val skipInMarkdownContext: Boolean = true

    override suspend fun TransformerContext.transform(match: MatchResult): String? {
        val (channelId) = match.destructured
        return try {
            val channel = kord.getChannelOf<GuildChannel>(Snowflake(channelId)) ?: return null
            "#${channel.name}"
        } catch (e: NumberFormatException) {
            null
        }
    }
}
