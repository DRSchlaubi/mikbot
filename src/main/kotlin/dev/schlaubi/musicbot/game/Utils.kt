package dev.schlaubi.musicbot.game

import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.schlaubi.musicbot.module.settings.BotUser
import dev.schlaubi.musicbot.utils.MessageBuilder
import kotlin.reflect.KProperty1

suspend fun AbstractGame<*>.confirmation(ack: EphemeralInteractionResponseBehavior, messageBuilder: MessageBuilder) =
    dev.schlaubi.musicbot.utils.confirmation(
        {
            ack.followUpEphemeral { it() }
        },
        messageBuilder = messageBuilder,
        translate = translationsProvider::translate
    )

suspend fun <T : Player> AbstractGame<T>.update(
    player: T,
    stats: KProperty1<BotUser, GameStats?>,
    copy: BotUser.(stats: GameStats) -> BotUser,
    updaterFunction: GameStats.() -> GameStats
) {
    val user = database.users.findOneById(player.user.id) ?: BotUser(player.user.id)
    val existingStats = stats.get(user) ?: GameStats(0, 0, 0.0)
    val newStats = existingStats.updaterFunction()

    database.users.save(user.copy(newStats))
}

/**
 * Translates [key] for a game.
 */
@Suppress("UNCHECKED_CAST")
fun AbstractGame<*>.translate(key: String, vararg replacements: Any?) =
    translationsProvider.translate(
        key, SupportedLocales.ENGLISH,
        bundle, replacements as Array<Any?>
    )
