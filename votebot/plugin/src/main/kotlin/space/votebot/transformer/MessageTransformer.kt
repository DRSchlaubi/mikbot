package space.votebot.transformer

import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior

/**
 * Interface which allows several implementations to replace things in user provided vote texts.
 * This allows us to replace things like user or channel mentions with the correct text.
 */
interface MessageTransformer {

    /**
     * Transforms the message
     */
    suspend fun transform(message: String, context: TransformerContext): String
}

private val transformers: List<MessageTransformer> =
    listOf(ChannelMessageTransformer, UserMessageTransformer, RoleMessageTransformer)

/**
 * Transforms the message using the transformer pipeline.
 */
suspend fun transformMessageSafe(message: String, context: TransformerContext): String {
    return transformers.fold(message) { acc, transformer ->
        try {
            transformer.transform(acc, context)
        } catch (e: Exception) {
            acc
        }
    }
}

class TransformerContext(
    val guild: GuildBehavior,
    val kord: Kord,
    val inMarkdownContext: Boolean
)
