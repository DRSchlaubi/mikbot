package dev.schlaubi.mikbot.game.api

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.stdx.coroutines.SuspendLazy
import kotlinx.coroutines.CoroutineScope
import java.util.*

/**
 * Representation of a game.
 *
 * @property players a list of all players in the game, which are still playing
 * @property module the [GameModule] using this game
 * @property translationsProvider the [TranslationsProvider] used for translations
 * @property locale the locale the game uses
 * @property thread the [ThreadChannelBehavior] the game is in
 * @property bundle the translation bundle name
 */
interface Game<T : Player> : CoroutineScope {
    val players: MutableList<T>
    val module: GameModule<T, AbstractGame<T>>
    val translationsProvider: TranslationsProvider
    val locale: SuspendLazy<Locale>
    val thread: ThreadChannelBehavior
    val bundle: String get() = module.bundle
}
