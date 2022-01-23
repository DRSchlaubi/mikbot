package dev.schlaubi.mikbot.game.api

import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.schlaubi.mikbot.plugin.api.util.MessageBuilder
import dev.schlaubi.mikbot.plugin.api.util.getLocale
import java.util.*

suspend fun AbstractGame<*>.confirmation(
    ack: EphemeralInteractionResponseBehavior,
    hasNoOption: Boolean = true,
    messageBuilder: MessageBuilder
) =
    dev.schlaubi.mikbot.plugin.api.util.confirmation(
        {
            ack.followUpEphemeral { it() }
        },
        hasNoOption = hasNoOption,
        messageBuilder = messageBuilder,
        translate = translationsProvider::translate
    )

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
fun AbstractGame<*>.translate(key: String, vararg replacements: Any?, locale: Locale = SupportedLocales.ENGLISH) =
    translationsProvider.translate(
        key, locale,
        bundle, replacements = replacements as Array<Any?>
    )

suspend fun AbstractGame<*>.translate(user: UserBehavior, key: String, vararg replacements: Any?): String {
    val locale = module.bot.getLocale(thread.asChannel(), user.asUser())
    return translate(key, locale = locale, replacements = replacements)
}

@Suppress("UNCHECKED_CAST")
internal suspend fun AbstractGame<*>.translateInternally(
    user: UserBehavior,
    key: String,
    vararg replacements: Any?
): String {
    val locale = module.bot.getLocale(thread.asChannel(), user.asUser())
    return translationsProvider.translate(
        key, locale,
        "games", replacements = replacements as Array<Any?>
    )
}
