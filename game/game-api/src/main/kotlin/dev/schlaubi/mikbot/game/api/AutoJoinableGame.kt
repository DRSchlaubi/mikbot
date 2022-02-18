package dev.schlaubi.mikbot.game.api

import dev.kord.core.entity.User

/**
 * A game which can add the owner automatically.
 *
 * **For [ControlledGame]** use [ControlledGame.supportsAutoJoin]
 */
interface AutoJoinableGame<T : Player> : Game<T> {
    /**
     * Obtains a new player for [user], without controls.
     *
     * @see ControlledGame
     */
    fun obtainNewPlayer(user: User): T
}
