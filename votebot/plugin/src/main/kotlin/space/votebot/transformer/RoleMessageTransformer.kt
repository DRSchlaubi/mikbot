package space.votebot.transformer

import dev.kord.common.entity.Snowflake

object RoleMessageTransformer : RegexReplaceTransformer() {
    override val regex: Regex = Regex("<@&(\\d+)>")
    override val skipInMarkdownContext: Boolean = true

    override suspend fun TransformerContext.transform(match: MatchResult): String? {
        return try {
            val (userId) = match.destructured
            guild.getRoleOrNull(Snowflake(userId))?.name?.let { roleName ->
                "@$roleName"
            }
        } catch (e: NumberFormatException) {
            null
        }
    }
}
