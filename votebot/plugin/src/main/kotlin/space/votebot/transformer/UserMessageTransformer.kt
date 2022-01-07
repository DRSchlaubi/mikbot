package space.votebot.transformer

import dev.kord.common.entity.Snowflake
import dev.kord.common.exception.RequestException
import dev.kord.core.Kord
import dev.kord.core.entity.User
import dev.kord.core.exception.EntityNotFoundException
import mu.KotlinLogging

object UserMessageTransformer : RegexReplaceTransformer() {
    override val regex = Regex("<@!?(\\d+)>")
    override val skipInMarkdownContext: Boolean = true

    override suspend fun TransformerContext.transform(match: MatchResult): String? {
        val (userId) = match.destructured
        val user = kord.tryGetUser(userId) ?: return null
        return "${user.username}#${user.discriminator}"
    }
}

private val logger = KotlinLogging.logger {}

private suspend fun Kord.tryGetUser(id: String): User? {
    return try {
        getUser(Snowflake(id))
    } catch (e: NumberFormatException) {
        null
    } catch (e: EntityNotFoundException) {
        null
    } catch (e: RequestException) {
        logger.error(e) { "An error occurred while trying to get the user ${id}. This shouldn't happen!" }
        null
    }
}
