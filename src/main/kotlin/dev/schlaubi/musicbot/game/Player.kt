package dev.schlaubi.musicbot.game

import dev.kord.core.behavior.UserBehavior

/**
 * Representation of a game player.
 *
 * @property user the [UserBehavior] of the user playing
 */
interface Player {
    val user: UserBehavior
}
