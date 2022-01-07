package space.votebot.transformer

import dev.kord.common.entity.Snowflake

object UserMessageTransformer : RegexReplaceTransformer() {
    override val regex = Regex("<@!?(\\d+)>")
    override val skipInMarkdownContext: Boolean = true

    override suspend fun TransformerContext.transform(match: MatchResult): String? {
        val (userId) = match.destructured
        return try {
            val user = kord.getUser(Snowflake(userId)) ?: return null
            return "${user.username}#${user.discriminator}"
        } catch (e: NumberFormatException) {
            null
        }
    }
}
