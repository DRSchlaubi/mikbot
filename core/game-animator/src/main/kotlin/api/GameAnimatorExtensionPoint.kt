/* ktlint-disable package-name */
package dev.schlaubi.mikbot.core.game_animator.api

import org.pf4j.ExtensionPoint

/**
 * Extension point for game animator plugin.
 */
interface GameAnimatorExtensionPoint : ExtensionPoint {
    /**
     * Replace variables in a game before sending it to Discord.
     */
    suspend fun String.replaceVariables(): String
}
