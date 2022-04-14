package dev.schlaubi.mikbot.game.api

import com.kotlindiscord.kord.extensions.utils.getJumpUrl
import dev.kord.common.Locale
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.behavior.interaction.response.followUp
import dev.kord.core.entity.interaction.followup.FollowupMessage
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.rest.builder.message.modify.actionRow
import dev.schlaubi.mikbot.plugin.api.util.componentLive
import dev.schlaubi.mikbot.plugin.api.util.convertToISO
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.minutes
import java.util.Locale as JavaLocale

/**
 * An [AbstractGame] helper for games with ephemeral controls.
 *
 * @property players all players
 */
interface ControlledGame<P : ControlledPlayer> : Game<P> {
    val running: Boolean
    val supportsAutoJoin get() = true

    /**
     * Adds the resend controlls button.
     */
    suspend fun MessageModifyBuilder.addResendControlsButton() {
        if (running) {
            actionRow {
                interactionButton(ButtonStyle.Secondary, resendControlsButton) {
                    label = translateInternally(key = "game.ui.resend_controls")
                }
            }
        }
    }

    @OptIn(KordUnsafe::class)
    suspend fun AbstractGame<P>.askForRematch(newThread: ThreadChannelBehavior, newGame: AbstractGame<P>): Boolean {
        val waitingPlayers = players.map { it.user }.toMutableList()

        fun content(): String = """In order to start this rematch, please click this button to obtain your controls
                    |Still waiting for: ${waitingPlayers.joinToString(", ") { it.mention }}
                    """.trimMargin()

        fun ActionRowBuilder.requestButton() {
            interactionButton(ButtonStyle.Success, resendControlsButton) {
                label = "Request Controls"
            }
        }

        val message = newThread.createMessage {
            content = content()

            actionRow {
                requestButton()
            }
        }
        thread.createMessage("Please request your controls for the rematch here: ${message.getJumpUrl()}")
        val completer = CompletableDeferred<Unit>()

        message.componentLive(this).onInteraction {
            val ack = interaction.deferEphemeralResponseUnsafe()
            val newPlayer = newGame.obtainNewPlayer(
                interaction.user,
                ack,
                ack.createEphemeralFollowup { content = "Loading ..." },
                interaction.locale
            )
            newGame.players.add(newPlayer)
            waitingPlayers.remove(newPlayer.user)
            if (waitingPlayers.isEmpty()) {
                completer.complete(Unit)
            }
            message.edit {
                content = content()

                actionRow {
                    requestButton()
                }
            }
        }

        val response = withTimeoutOrNull(1.minutes) {
            completer.await()
        }

        message.delete()

        if (response == null) {
            newGame.players.forEach {
                it.controls.edit { content = "This controls died!" }
            }
            thread.createMessage("Unfortunately we couldn't get all players to re-join")
            return false
        }

        return true
    }
}

/**
 * interface for a [Player] with custom controls.
 *
 * @property ack the [MessageInteractionResponseBehavior] providing the [controls] follow-up
 * @property controls the actual controls
 */
interface ControlledPlayer : Player {
    val ack: MessageInteractionResponseBehavior
    val controls: FollowupMessage
    val discordLocale: Locale?
    val locale: JavaLocale?
        get() = discordLocale?.convertToISO()?.asJavaLocale()
    val game: AbstractGame<*>

    /**
     * Requests new controls for this placer.
     */
    suspend fun resendControls(ack: EphemeralMessageInteractionResponseBehavior)
}
