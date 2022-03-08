package dev.schlaubi.mikbot.game.connect_four.game

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.utils.waitFor
import dev.kord.common.Locale
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.followup.FollowupMessage
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.schlaubi.mikbot.game.api.AbstractGame
import dev.schlaubi.mikbot.game.api.Rematchable
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.connect_four.Connect4
import dev.schlaubi.mikbot.game.connect_four.WinResult
import java.util.*

class Connect4Game(
    height: Int,
    width: Int,
    connect: Int,
    override val thread: ThreadChannelBehavior,
    override val welcomeMessage: Message,
    override val translationsProvider: TranslationsProvider,
    host: UserBehavior,
    module: GameModule<Connect4Player, AbstractGame<Connect4Player>>,
    private val lastWinner: Connect4Player? = null
) : AbstractGame<Connect4Player>(host, module), Rematchable<Connect4Player, Connect4Game> {

    override val rematchThreadName: String = "connect4-rematch"
    override val playerRange: IntRange = 2 until 3

    internal val possibleTypes by lazy { LinkedList(Connect4.Player.values().toList()) }
    val game by lazy { Connect4(height, width, connect) }
    override val isEligibleForStats: Boolean get() = game.isEligible
    private val playerCycle = PlayerCycle()

    var winResult: WinResult? = null

    override val wonPlayers: List<Connect4Player>
        get() = if (winResult is WinResult.Winner) {
            listOf(
                players.first {
                    it.type == (winResult as WinResult.Winner).player
                }
            )
        } else {
            emptyList()
        }

    override suspend fun runGame() {
        while (winResult == null) {
            val player = playerCycle.next()
            updateBoard(player, false)

            val component = kord.waitFor<GuildButtonInteractionCreateEvent> {
                if (interaction.message == welcomeMessage) {
                    interaction.deferEphemeralMessageUpdate()
                    interaction.user == player.user
                } else {
                    false
                }
            }!!.interaction.componentId
            val rowId = component.substringAfter("select_").toInt()
            game.place(player.type, rowId)

            winResult = game.determineWinner()
        }
    }

    override suspend fun EmbedBuilder.endEmbed(messageModifyBuilder: MessageModifyBuilder) {
        messageModifyBuilder.apply {
            content = if (winResult !is WinResult.Draw) {
                buildGameBoard()
            } else {
                "You managed to NOT win, seriously how did you manage to mess this up, this is ridiculous how bad you are"
            }
        }
    }

    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralMessageInteractionResponseBehavior,
        loading: FollowupMessage,
        userLocale: Locale?
    ): Connect4Player {
        val player = Connect4Player(user, possibleTypes.poll())
        loading.edit {
            translationsProvider.translate(
                "game.controls.joined",
                module.bundle,
                userLocale?.country ?: "en",
                arrayOf(player.type.getEmoji())
            )
        }

        return player
    }

    override suspend fun rematch(thread: ThreadChannelBehavior, welcomeMessage: Message): Connect4Game {
        return Connect4Game(
            game.height,
            game.width,
            game.connect,
            thread,
            welcomeMessage,
            translationsProvider,
            host,
            module,
            (winResult as? WinResult.Winner)?.player?.let { winner -> players.first { it.type == winner } }
        ).apply {
            players.addAll(this@Connect4Game.players)
        }
    }

    inner class PlayerCycle : Iterator<Connect4Player> {
        private var nextIndex = lastWinner?.let { players.indexOfFirst { player -> player.type == it.type } } ?: 1
        override fun hasNext(): Boolean = true

        override fun next(): Connect4Player {
            if (nextIndex >= players.lastIndex) {
                nextIndex = 0
            } else {
                nextIndex++
            }
            return players[nextIndex]
        }
    }
}

private val Connect4.isEligible: Boolean
    get() = height == 6 && width == 7 && connect == 4
