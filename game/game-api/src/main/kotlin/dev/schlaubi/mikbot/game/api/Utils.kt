package dev.schlaubi.mikbot.game.api

import dev.kord.core.behavior.interaction.InteractionResponseBehavior
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.schlaubi.mikbot.plugin.api.util.MessageBuilder
import dev.schlaubi.mikbot.plugin.api.util.getLocale
import java.util.*

suspend fun AbstractGame<*>.confirmation(
    ack: InteractionResponseBehavior,
    hasNoOption: Boolean = true,
    locale: Locale? = null,
    messageBuilder: MessageBuilder
) =
    dev.schlaubi.mikbot.plugin.api.util.confirmation(
        {
            ack.followUpEphemeral { it() }
        },
        hasNoOption = hasNoOption,
        messageBuilder = messageBuilder,
        translate = { key, group ->
            translationsProvider.translate(key, locale ?: translationsProvider.defaultLocale, group)
        }
    )

suspend fun ControlledPlayer.confirmation(
    hasNoOption: Boolean = true,
    messageBuilder: MessageBuilder
) = game.confirmation(ack, hasNoOption, locale, messageBuilder)

suspend fun ControlledPlayer.translate(key: String, vararg replacements: Any?) =
    game.translate(key, *replacements, discordLocale)

suspend fun <T : Player> AbstractGame<T>.update(
    player: T,
    updaterFunction: GameStats.() -> GameStats
) {
    val userStats =
        module.gameStats.findOneById(player.user.id) ?: UserGameStats(player.user.id, GameStats(0, 0, 0.0, 0))
    val newStats = userStats.copy(stats = userStats.stats.updaterFunction())

    module.gameStats.save(newStats)
}

/**
 * Translates [key] for a game.
 */
@Suppress("UNCHECKED_CAST")
suspend fun AbstractGame<*>.translate(
    key: String,
    vararg replacements: Any?,
    locale: Locale? = null
) =
    translationsProvider.translate(
        key, locale ?: locale(),
        bundle, replacements = replacements as Array<Any?>
    )

suspend fun AbstractGame<*>.translate(user: Player, key: String, vararg replacements: Any?): String {
    return translate(key, locale = getLocale(user), replacements = replacements)
}

@Suppress("UNCHECKED_CAST")
suspend fun AbstractGame<*>.translateInternally(
    user: Player,
    key: String,
    vararg replacements: Any?
): String = translateInternally(
    getLocale(user), key, *replacements
)

private suspend fun AbstractGame<*>.getLocale(user: Player) =
    (user as? ControlledPlayer)?.locale ?: module.bot.getLocale(
        thread.asChannel(),
        user.user.asUser()
    )

@Suppress("UNCHECKED_CAST")
fun AbstractGame<*>.translateInternally(
    locale: Locale? = null,
    key: String,
    vararg replacements: Any?
): String {
    return translationsProvider.translate(
        key, locale ?: translationsProvider.defaultLocale,
        "games", replacements = replacements as Array<Any?>
    )
}
