package space.votebot.transformer

import dev.kord.core.Kord

/**
 * Interface which allows several implementations to replace things in user provided vote texts.
 * This allows us to replace things like user or channel mentions with the correct text.
 */
interface MessageTransformer {

    /**
     * Transforms the message
     */
    suspend fun transform(message: String, kord: Kord, inMarkdownContext: Boolean): String

}

private val transformers: List<MessageTransformer> = listOf(ChannelMessageTransformer, UserMessageTransformer)

/**
 * Transforms the message using the transformer pipeline.
 */
suspend fun transformMessage(message: String, kord: Kord, inMarkdownContext: Boolean = false): String {
    return transformers.fold(message) { acc, transformer ->
        transformer.transform(acc, kord, inMarkdownContext)
    }
}
