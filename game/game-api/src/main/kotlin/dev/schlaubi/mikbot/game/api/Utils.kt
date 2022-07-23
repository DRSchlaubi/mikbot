package dev.schlaubi.mikbot.game.api

import com.kotlindiscord.kord.extensions.commands.Command
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.schlaubi.mikbot.plugin.api.util.MessageBuilder
import dev.schlaubi.mikbot.plugin.api.util.getLocale
import java.util.*

suspend fun Game<*>.confirmation(
    ack: MessageInteractionResponseBehavior,
    hasNoOption: Boolean = true,
    locale: Locale? = null,
    messageBuilder: MessageBuilder
) =
    dev.schlaubi.mikbot.plugin.api.util.confirmation(
        {
            ack.createEphemeralFollowup {
                it()
            }
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
suspend fun Game<*>.translate(
    key: String,
    vararg replacements: Any?,
    locale: Locale? = null
) =
    translationsProvider.translate(
        key, locale ?: locale(),
        bundle, replacements = replacements as Array<Any?>
    )

suspend fun Game<*>.translate(user: Player, key: String, vararg replacements: Any?): String =
    translate(key, locale = getLocale(user), replacements = replacements)

@Suppress("UNCHECKED_CAST")
suspend fun Game<*>.translateInternally(
    user: Player,
    key: String,
    vararg replacements: Any?
): String = translateInternally(
    getLocale(user), key, *replacements
)

private suspend fun Game<*>.getLocale(user: Player) =
    (user as? ControlledPlayer)?.locale ?: module.bot.getLocale(
        thread.asChannel(),
        user.user.asUser()
    )

@Suppress("UNCHECKED_CAST")
suspend fun Game<*>.translateInternally(
    locale: Locale? = null,
    key: String,
    vararg replacements: Any?
): String {
    return translationsProvider.translate(
        key, locale ?: this.locale(),
        "games", replacements = replacements as Array<Any?>
    )
}

internal fun Command.setGameApiBundle() {
    bundle = "games"
}
