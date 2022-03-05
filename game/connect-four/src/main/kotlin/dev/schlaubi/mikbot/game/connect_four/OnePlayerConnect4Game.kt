package dev.schlaubi.mikbot.game.connect_four

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.Locale
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.response.EphemeralInteractionResponseBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.followup.FollowupMessage
import dev.schlaubi.mikbot.game.api.AbstractGame
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.connect_four.game.Connect4Player
import dev.schlaubi.mikbot.game.connect_four.game.buildGameBoard
import dev.schlaubi.mikbot.game.connect_four.game.getEmoji
import dev.schlaubi.mikbot.game.connect_four.game.mention
import dev.schlaubi.mikbot.game.google_emotes.googleLogoColor
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val botPlayerType = Connect4.Player.RED

/**
 * Joke version of the game, to make fun of people who try to play with [Connect4.connect] = 1.
 */
class OnePlayerConnect4Game(
    private val height: Int,
    private val width: Int,
    override val thread: ThreadChannelBehavior,
    override val welcomeMessage: Message,
    override val translationsProvider: TranslationsProvider,
    host: UserBehavior,
    module: GameModule<Connect4Player, AbstractGame<Connect4Player>>
) :
    AbstractGame<Connect4Player>(host, module) {
    override val playerRange: IntRange = 0..Int.MAX_VALUE

    @OptIn(KordUnsafe::class, KordExperimental::class)
    override val wonPlayers: List<Connect4Player> =
        listOf(Connect4Player(host.kord.unsafe.user(host.kord.selfId), botPlayerType))

    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralInteractionResponseBehavior,
        loading: FollowupMessage,
        userLocale: Locale?
    ): Connect4Player = Connect4Player(user, Connect4.Player.YELLOW)

    override suspend fun runGame() {
        welcomeMessage.edit { components = mutableListOf() }
        thread.createMessage("Oh this looks like a cool Game, I want to play too!")

        val game = Connect4(height, width, 1)
        val board = thread.createMessage(game.buildGameBoard())
        delay(1.seconds)
        thread.createMessage("I hope I can do this")
        delay(600.milliseconds)
        game.place(botPlayerType, 0)
        board.edit {
            content = game.buildGameBoard().replace(Connect4.Player.RED.getEmoji().mention, googleLogoColor.mention)
        }
        thread.createMessage("Wait, did I win?")
        delay(600.milliseconds)
        thread.createMessage("I DID?!")
        delay(300.milliseconds)
        thread.createMessage("Mom, are you proud of me?")
    }
}
