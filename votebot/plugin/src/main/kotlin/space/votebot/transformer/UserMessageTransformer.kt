package space.votebot.transformer

import dev.kord.common.entity.Snowflake
import dev.kord.common.exception.RequestException
import dev.kord.core.Kord
import dev.kord.core.entity.User
import dev.kord.core.exception.EntityNotFoundException
import mu.KotlinLogging

object UserMessageTransformer : MessageTransformer {
    private val regex = Regex("<@!?(\\d+)>")

    override suspend fun transform(message: String, kord: Kord, inMarkdownContext: Boolean): String {
        if (inMarkdownContext) {
            return message
        }
        return regex.findAll(message).toSet().mapNotNull { match ->
            val (userId) = match.destructured
            val user = kord.tryGetUser(userId) ?: return@mapNotNull null
            match.value to "${user.username}#${user.discriminator}"
        }.fold(message) { acc, (value, name) ->
            acc.replace(value, "@$name")
        }
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
