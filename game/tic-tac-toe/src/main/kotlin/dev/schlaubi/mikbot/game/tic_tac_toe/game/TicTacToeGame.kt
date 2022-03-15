package dev.schlaubi.mikbot.game.tic_tac_toe.game

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.utils.waitFor
import dev.kord.common.Locale
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.behavior.interaction.response.followUpEphemeral
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.followup.FollowupMessage
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.schlaubi.mikbot.game.api.AbstractGame
import dev.schlaubi.mikbot.game.api.AutoJoinableGame
import dev.schlaubi.mikbot.game.api.Rematchable
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikbot.game.tic_tac_toe.Coordinate
import dev.schlaubi.mikbot.game.tic_tac_toe.PlayerType
import dev.schlaubi.mikbot.game.tic_tac_toe.TicTacToe
import dev.schlaubi.mikbot.game.tic_tac_toe.WinResult
import java.util.*
import kotlin.time.Duration.Companion.minutes

class TicTacToeGame(
    private val lastWinner: PlayerType?,
    private val size: GameSize,
    override val thread: ThreadChannelBehavior,
    override val welcomeMessage: Message,
    override val translationsProvider: TranslationsProvider,
    host: UserBehavior,
    module: GameModule<TicTacToePlayer, AbstractGame<TicTacToePlayer>>
) : AbstractGame<TicTacToePlayer>(host, module), Rematchable<TicTacToePlayer, TicTacToeGame>, AutoJoinableGame<TicTacToePlayer> {
    override val rematchThreadName: String = "tic-tac-toe-rematch"
    private val playerTypeOrder = LinkedList(PlayerType.values().toList().shuffled())
    override val playerRange: IntRange = 2 until 3
    private val game = TicTacToe(size.size)
    private val cycle = PlayerCycle()

    private lateinit var winner: WinResult

    override val wonPlayers: List<TicTacToePlayer>
        get() = if (::winner.isInitialized && winner is WinResult.Winner) listOf(players.first { it.type == (winner as? WinResult.Winner)?.type }) else emptyList()

    override fun obtainNewPlayer(user: User): TicTacToePlayer = TicTacToePlayer(user, playerTypeOrder.poll())

    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralMessageInteractionResponseBehavior,
        loading: FollowupMessage,
        userLocale: Locale?
    ): TicTacToePlayer {
        val type = playerTypeOrder.poll()
        val player = TicTacToePlayer(user, type)
        ack.createEphemeralFollowup {

            content = translate(
                player, "tic_tac_toe.controls.joined", type
            )
        }
        return player
    }

    override suspend fun onRejoin(event: ComponentInteractionCreateEvent, player: TicTacToePlayer) {
        event.interaction.deferEphemeralMessageUpdate()
    }

    override suspend fun runGame() {
        val nextPlayer = cycle.next()
        welcomeMessage.edit {
            embeds = mutableListOf()
            content = "It's ${nextPlayer.user.mention}'s turn (${nextPlayer.type.emoji})"
            addTicTacToe(game)
        }

        val turn = kord.waitFor<ComponentInteractionCreateEvent>(1.minutes.inWholeMilliseconds) {
            if (interaction.message == welcomeMessage) {
                interaction.deferEphemeralMessageUpdate()
                interaction.user == nextPlayer.user
            } else {
                false
            }
        }

        fun determineCoordinates(): Coordinate {
            if (turn != null) {
                // Kt compiler screwing up :D
                val (x, y) = turn.interaction.componentId.substringAfter("select_").split('_').map { it.toInt() }
                return Coordinate(x, y)
            } else {
                repeat(game.size) { x ->
                    repeat(game.size) { y ->
                        if (game.isFree(x, y)) {
                            return Coordinate(x, y)
                        }
                    }
                }

                error("Hey, this is odd")
            }
        }
        val (x, y) = determineCoordinates()
        game.place(x, y, nextPlayer.type)

        winner = game.determineWinner()
        if (winner == WinResult.NoWinnerYet) {
            return runGame() // do next turn
        }
    }

    override suspend fun rematch(thread: ThreadChannelBehavior, welcomeMessage: Message): TicTacToeGame {
        return TicTacToeGame(
            (winner as? WinResult.Winner)?.type,
            size, thread, welcomeMessage, translationsProvider, host, module
        ).apply {
            players.addAll(this@TicTacToeGame.players)
        }
    }

    override fun MessageModifyBuilder.modifyEndMessage() {
        if (winner is WinResult.Winner) {
            addTicTacToeWinner(game, winner as WinResult.Winner)
        } else {
            embeds = mutableListOf()
            content =
                "Sooo yeah in order to answer who won, we will ask the almighty MAGIC 8 BALL, therewego its's contacting the oracle, this might just take a bit: ||AND IT'S A NOOOOOOOOOOOO||"
        }
    }

    inner class PlayerCycle : Iterator<TicTacToePlayer> {
        private var nextIndex = lastWinner?.let { type -> players.indexOfFirst { it.type == type } } ?: 0
        override fun hasNext(): Boolean = true

        override fun next(): TicTacToePlayer {
            if (nextIndex >= players.lastIndex) {
                nextIndex = 0
            } else {
                nextIndex++
            }
            return players[nextIndex]
        }
    }
}
