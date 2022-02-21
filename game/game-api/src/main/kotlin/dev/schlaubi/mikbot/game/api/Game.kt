package dev.schlaubi.mikbot.game.api

import dev.schlaubi.mikbot.game.api.module.GameModule
import kotlinx.coroutines.CoroutineScope

/**
 * Representation of a game.
 *
 * @property players a list of all players in the game, which are still playing
 * @property module the [GameModule] using this game
 */
interface Game<T : Player> : CoroutineScope {
    val players: MutableList<T>
    val module: GameModule<T, AbstractGame<T>>
}
