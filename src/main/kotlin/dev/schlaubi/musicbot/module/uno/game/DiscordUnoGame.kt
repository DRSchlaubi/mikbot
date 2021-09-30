package dev.schlaubi.musicbot.module.uno.game

import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.core.Kord
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.channel.threads.edit
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.embed
import dev.kord.x.emoji.Emojis
import dev.schlaubi.musicbot.core.io.Database
import dev.schlaubi.musicbot.module.settings.BotUser
import dev.schlaubi.musicbot.module.settings.UnoStats
import dev.schlaubi.musicbot.module.uno.game.player.DiscordUnoPlayer
import dev.schlaubi.musicbot.module.uno.game.player.translate
import dev.schlaubi.musicbot.module.uno.game.player.updateControls
import dev.schlaubi.musicbot.module.uno.game.ui.updateWelcomeMessage
import dev.schlaubi.musicbot.utils.MessageBuilder
import dev.schlaubi.uno.Game
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

const val joinGameButton = "join_game"
const val resendControlsButton = "resend_controls"
const val startGameButton = "start_game"

class DiscordUnoGame(
    val host: UserBehavior,
    val welcomeMessage: Message,
    val thread: ThreadChannelBehavior,
    val translationsProvider: TranslationsProvider,
) : KoinComponent {
    val database: Database by inject()
    val players = mutableListOf<DiscordUnoPlayer>()
    val kord: Kord get() = host.kord
    var running = false
    lateinit var game: Game<DiscordUnoPlayer>
        internal set

    private val interactionListener = interactionHandler()
    private val threadWatcher = watchThread()
    internal var currentTurn: Job? = null
    internal var currentPlayer: DiscordUnoPlayer? = null

    fun removePlayer(player: DiscordUnoPlayer) {
        players.remove(player)
        if (running) {
            game.removePlayer(player)
        }
    }

    suspend fun startGame() {
        running = true
        updateWelcomeMessage()

        players.forEach {
            it.updateControls(false)
        }

        while (game.gameRunning) {
            coroutineScope {
                currentTurn = launch {
                    currentPlayer = game.nextPlayer()
                    currentPlayer!!.turn()
                    updateWelcomeMessage()
                }
            }
        }

        end()
    }

    suspend fun end() {
        players.forEach {
            it.controls.edit {
                components = mutableListOf()
                content = it.translate("uno.controls.ended")
            }
        }

        if (players.size == 1) {
            game.forceWin(players.first())
        }

        updateStats()

        welcomeMessage.edit {
            components = mutableListOf()
            embed {
                title = "UNO - Game ended"
                if (running && game.wonPlayers.isNotEmpty()) {
                    repeat(3) {
                        winner(it)
                    }
                } else {
                    description = "The game ended abruptly"
                }
            }
        }

        thread.edit {
            archived = true
        }
        interactionListener.cancel()
        threadWatcher.cancel()
    }

    private suspend fun updateStats() {
        val winner = game.wonPlayers.firstOrNull()
        if (winner != null) {
            winner.update {
                copy(
                    wins = wins + 1,
                    ratio = (wins + 1).toDouble().div(losses.coerceAtLeast(1))
                )
            }

            ((game.wonPlayers - winner) + players).forEach {
                it.update {
                    copy(
                        losses = losses + 1,
                        ratio = wins.toDouble().div((losses + 1))
                    )
                }
            }
        }
    }

    private suspend fun DiscordUnoPlayer.update(updaterFunction: UnoStats.() -> UnoStats) {
        val user = database.users.findOneById(owner.id) ?: BotUser(owner.id)
        val stats = user.unoStats ?: UnoStats(0, 0, 0.0)
        val newStats = stats.updaterFunction()

        database.users.save(user.copy(unoStats = newStats))
    }

    private fun EmbedBuilder.winner(placeIndex: Int) {
        val player = game.wonPlayers.getOrNull(placeIndex) ?: return

        description = "Winners: "

        val medal = when (placeIndex) {
            0 -> Emojis.firstPlace
            1 -> Emojis.secondPlace
            2 -> Emojis.thirdPlace
            else -> error("Invalid place: $placeIndex")
        }

        description += "$medal ${player.owner.mention}"
    }

    suspend fun confirmation(ack: EphemeralInteractionResponseBehavior, messageBuilder: MessageBuilder) =
        dev.schlaubi.musicbot.utils.confirmation({
            ack.followUpEphemeral { it() }
        }, messageBuilder = messageBuilder, translate = translationsProvider::translate)

    val ComponentInteraction.unoPlayer: DiscordUnoPlayer?
        get() = players.firstOrNull { it.owner == user }

    @Suppress("UNCHECKED_CAST")
    fun translate(key: String, vararg replacements: Any?) =
        translationsProvider.translate(
            key, SupportedLocales.ENGLISH,
            "uno", replacements as Array<Any?>
        )
}
