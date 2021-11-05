package dev.schlaubi.mikbot.game.api

import dev.kord.core.behavior.UserBehavior

/**
 * Representation of a game player.
 *
 * @property user the [UserBehavior] of the user playing
 */
interface Player {
    val user: UserBehavior
}
