package space.votebot.transformer

/**
 * Transformer which replaces everything in a message which matches the specified [regex].
 */
abstract class RegexReplaceTransformer : MessageTransformer {
    abstract val regex: Regex
    abstract val skipInMarkdownContext: Boolean

    abstract suspend fun TransformerContext.transform(match: MatchResult): String?

    override suspend fun transform(message: String, context: TransformerContext): String {
        if (context.inMarkdownContext && skipInMarkdownContext) {
            return message
        }
        return regex.findAll(message).toSet().mapNotNull { match ->
            match.value to (context.transform(match) ?: return@mapNotNull null)
        }.fold(message) { acc, (value, replacement) ->
            acc.replace(value, replacement)
        }
    }
}
