package dev.schlaubi.mikbot.game.uno.game

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.FollowupMessage
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.rest.builder.message.modify.embed
import dev.schlaubi.mikbot.game.api.AbstractGame
import dev.schlaubi.mikbot.game.api.ControlledGame
import dev.schlaubi.mikbot.game.api.Rematchable
import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikbot.game.uno.UnoModule
import dev.schlaubi.mikbot.game.uno.game.player.DiscordUnoPlayer
import dev.schlaubi.mikbot.game.uno.game.player.updateControls
import dev.schlaubi.mikbot.game.uno.game.ui.welcomeMessage
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.uno.Game
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val LOG = KotlinLogging.logger { }

class DiscordUnoGame(
    host: UserBehavior,
    module: UnoModule,
    override val welcomeMessage: Message,
    override val thread: ThreadChannelBehavior,
    override val translationsProvider: TranslationsProvider,
    private val extremeMode: Boolean,
    val flashMode: Boolean,
    val allowDropIns: Boolean
) : AbstractGame<DiscordUnoPlayer>(host, module), ControlledGame<DiscordUnoPlayer>, Rematchable<DiscordUnoGame> {
    override val rematchThreadName: String = "uno-rematch"
    lateinit var game: Game<DiscordUnoPlayer>
        internal set
    override val wonPlayers: List<DiscordUnoPlayer>
        get() = game.wonPlayers

    var lastPlayer: DiscordUnoPlayer? = null
    val nextPlayer: DiscordUnoPlayer get() = game.getNextPlayer()

    override val playerRange: IntRange = 2..10
    internal var currentTurn: Job? = null
    internal var currentPlayer: DiscordUnoPlayer? = null

    override suspend fun removePlayer(player: DiscordUnoPlayer) = kickPlayer(player)

    override suspend fun MessageModifyBuilder.updateWelcomeMessage() {
        embeds?.clear()
        embed {
            addWelcomeMessage()
        }
    }

    override fun EmbedBuilder.addWelcomeMessage() {
        welcomeMessage(this@DiscordUnoGame)
    }

    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralInteractionResponseBehavior,
        loading: FollowupMessage
    ): DiscordUnoPlayer = DiscordUnoPlayer(
        user,
        ack,
        loading,
        this
    )

    override suspend fun onRejoin(event: ComponentInteractionCreateEvent, player: DiscordUnoPlayer) =
        player.resendControlsInternally(
            event,
            justLoading = true
        )

    override suspend fun runGame() {
        game = Game(players, extremeMode, flashMode)

        players.forEach {
            it.updateControls(false)
        }

        while (game.gameRunning) {
            lastPlayer = currentPlayer
            currentPlayer = game.nextPlayer()
            doUpdateWelcomeMessage()
            coroutineScope {
                currentTurn = launch {
                    try {
                        currentPlayer!!.turn()
                    } catch (e: Exception) {
                        currentPlayer!!.response.followUpEphemeral {
                            content = translate(currentPlayer!!.user, "uno.controls.failed")
                        }
                        currentPlayer!!.resendControlsInternally(null)
                        LOG.error(e) { "Error occurred whilst updating game" }
                    }
                    doUpdateWelcomeMessage()
                    if (allowDropIns) {
                        checkForDropIns()
                    }
                }
            }
            doUpdateWelcomeMessage()
        }
    }

    override suspend fun end() {
        if (!running) return

        if (players.size == 1) {
            game.forceWin(players.first())
        }

        if (flashMode) {
            thread.createEmbed {
                title = "Turns"
                description =
                    players.sortedBy(DiscordUnoPlayer::turns)
                        .joinToString("\n") { "${it.user.mention} - ${it.turns}" }
            }
        }
    }

    override suspend fun rematch(thread: ThreadChannelBehavior, welcomeMessage: Message): DiscordUnoGame {
        val game = DiscordUnoGame(
            host,
            module as UnoModule,
            welcomeMessage,
            thread,
            translationsProvider,
            extremeMode, flashMode, allowDropIns
        )
        if (!askForRematch(
                thread,
                game
            )
        ) {
            discordError("Game could not restart")
        }

        return game
    }
}
