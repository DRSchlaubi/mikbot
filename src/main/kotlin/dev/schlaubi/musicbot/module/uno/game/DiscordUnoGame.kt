package dev.schlaubi.musicbot.module.uno.game

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.EphemeralFollowupMessage
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import dev.schlaubi.musicbot.game.AbstractGame
import dev.schlaubi.musicbot.game.GameStats
import dev.schlaubi.musicbot.game.translate
import dev.schlaubi.musicbot.module.settings.BotUser
import dev.schlaubi.musicbot.module.uno.game.player.DiscordUnoPlayer
import dev.schlaubi.musicbot.module.uno.game.player.translate
import dev.schlaubi.musicbot.module.uno.game.player.updateControls
import dev.schlaubi.musicbot.module.uno.game.ui.welcomeMessage
import dev.schlaubi.uno.Game
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.reflect.KProperty1

const val joinGameButton = "join_game"
const val resendControlsButton = "resend_controls"
const val startGameButton = "start_game"

private val LOG = KotlinLogging.logger { }

class DiscordUnoGame(
    host: UserBehavior,
    override val welcomeMessage: Message,
    override val thread: ThreadChannelBehavior,
    override val translationsProvider: TranslationsProvider
) : AbstractGame<DiscordUnoPlayer>(host) {
    override val bundle: String = "uno"
    lateinit var game: Game<DiscordUnoPlayer>
        internal set
    override val wonPlayers: List<DiscordUnoPlayer>
        get() = game.wonPlayers

    override val playerRange: IntRange = 2..10
    internal var currentTurn: Job? = null
    override val statsProperty: KProperty1<BotUser, GameStats?> = BotUser::unoStats
    internal var currentPlayer: DiscordUnoPlayer? = null
    override fun BotUser.applyStats(stats: GameStats): BotUser = copy(unoStats = stats)

    override suspend fun removePlayer(player: DiscordUnoPlayer) = kickPlayer(player)

    override suspend fun MessageModifyBuilder.updateWelcomeMessage() {
        embeds?.clear()
        embed {
            addWelcomeMessage()
        }

        actionRow {
            if (running) {
                interactionButton(ButtonStyle.Secondary, resendControlsButton) {
                    label = "Resend Controls"
                }
            }
        }
    }

    override fun EmbedBuilder.addWelcomeMessage() {
        welcomeMessage(this@DiscordUnoGame)
    }

    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralInteractionResponseBehavior,
        loading: EphemeralFollowupMessage
    ): DiscordUnoPlayer = DiscordUnoPlayer(
        user,
        ack,
        loading,
        this
    )

    override suspend fun onRejoin(event: ComponentInteractionCreateEvent, player: DiscordUnoPlayer) =
        player.resendControls(
            event,
            justLoading = true,
            overrideConfirm = true
        )

    override suspend fun ComponentInteractionCreateEvent.onInteraction() {
        when (interaction.componentId) {
            resendControlsButton -> {
                val player = interaction.gamePlayer ?: return
                player.resendControls(this)
            }
        }
    }

    override suspend fun runGame() {
        game = Game(players)

        players.forEach {
            it.updateControls(false)
        }

        while (game.gameRunning) {
            doUpdateWelcomeMessage()
            coroutineScope {
                currentTurn = launch {
                    try {
                        currentPlayer = game.nextPlayer()
                        currentPlayer!!.turn()
                    } catch (e: Exception) {
                        currentPlayer!!.response.followUpEphemeral {
                            content = translate("uno.controls.failed")
                        }
                        currentPlayer!!.resendControls(null, overrideConfirm = true)
                        LOG.error(e) { "Error occurred whilst updating game" }
                    }
                    doUpdateWelcomeMessage()
                }
            }
            doUpdateWelcomeMessage()
        }
    }

    override suspend fun end() {
        players.forEach {
            it.controls.edit {
                components = mutableListOf()
                content = it.translate("uno.controls.ended")
            }
        }

        if (players.size == 1) {
            game.forceWin(players.first())
        }
    }
}
