package dev.schlaubi.mikbot.game.api

import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.entity.Message

/**
 * Interface for adding a "Rematch" button at the winner card
 */
interface Rematchable<P : Player, T : AbstractGame<P>> : Game<P> {
    /**
     * Name of a rematch thread.
     */
    val rematchThreadName: String

    /**
     * Creates a new rematch of the game in [thread] with [welcomeMessage].
     */
    suspend fun rematch(thread: ThreadChannelBehavior, welcomeMessage: Message): T
}
