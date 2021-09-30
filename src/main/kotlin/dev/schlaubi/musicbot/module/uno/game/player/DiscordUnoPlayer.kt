package dev.schlaubi.musicbot.module.uno.game.player

import com.kotlindiscord.kord.extensions.utils.waitFor
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.entity.interaction.EphemeralFollowupMessage
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.schlaubi.musicbot.module.uno.game.DiscordUnoGame
import dev.schlaubi.musicbot.utils.deleteAfterwards
import dev.schlaubi.uno.Player
import dev.schlaubi.uno.cards.AbstractWildCard
import kotlinx.coroutines.launch
import kotlin.time.Duration

internal val unoInteractionTimeout = Duration.seconds(30).inWholeMilliseconds

const val drawCardButton = "draw_card"
const val sayUnoButton = "say_uno"
const val skipButton = "skip"

class DiscordUnoPlayer(
    val owner: UserBehavior,
    val response: EphemeralInteractionResponseBehavior,
    var controls: EphemeralFollowupMessage,
    val game: DiscordUnoGame
) : Player() {
    private var myTurn = false
    internal var drawn = false

    suspend fun turn() {
        myTurn = true
        updateControls(true)
        if (drawn) {
            val cantPlay = deck.none { it.canBePlayedOn(game.game.topCard) }
            if (cantPlay) {
                return endTurn() // auto-skip if drawn and can't play
            }
        }

        val response = game.kord.waitFor<ComponentInteractionCreateEvent>(unoInteractionTimeout) {
            interaction.message?.id == controls.id && interaction.user == owner
        }

        myTurn = false // Prevent clicking again
        if (response != null) {
            response.interaction.acknowledgePublicDeferredMessageUpdate()
            if (normalPlay(response)) { // if the action calls for a new turn repeat
                return turn()
            }
        } else {
            aiPlay()
        }

        endTurn()
    }

    private suspend fun endTurn() {
        updateControls(false)
        drawn = false
    }

    private suspend fun normalPlay(response: ComponentInteractionCreateEvent): Boolean {
        val name = response.interaction.componentId
        @Suppress("DUPLICATE_LABEL_IN_WHEN") // not duplicated, we want to purposely not skip
        when (name) {
            drawCardButton -> {
                drawn = true
                draw(game.game)
                return true
            }
            sayUnoButton -> {
                playUno()
                return true
            }
            skipButton -> return false
            else -> {
                val cardId = name.substringAfter("play_card_").toInt()
                val card = deck[cardId]
                if (card is AbstractWildCard && deck.size != 1) { // ignore pick on last card
                    val color = pickWildCardColor()
                    playCard(game.game, card, color)
                } else {
                    playCard(game.game, card)
                }

                return false
            }
        }
    }

    private suspend fun playUno() {
        uno()
        val message = game.thread.createMessage("${owner.mention} just said UNO!")

        game.kord.launch {
            runCatching {
                // Ignore thread is archived errors
                message.deleteAfterwards(Duration.seconds(15))
            }
        }
    }

    private suspend fun aiPlay() {
        // Probably most horrible AI ever but just don't go afk
        val card = deck.firstOrNull { it.canBePlayedOn(game.game.topCard) }
        if (card != null) {
            if (deck.size == 2) {
                playUno()
            }

            playCard(game.game, card)
        } else {
            draw(game.game)
        }
    }

    suspend fun resendControls(event: ComponentInteractionCreateEvent) {
        val ack = event.interaction.acknowledgeEphemeral()
        val (confirmed) = game.confirmation(ack) {
            content = translate("uno.resend_controls.confirm")
        }
        if (confirmed) {
            controls.edit {
                content = translate("uno.controls.reset")
                components = mutableListOf()
            }
            game.thread.createMessage {
                content = translate("uno.resend_controls.blame", owner.mention)
            }.pin()

            controls = ack.followUpEphemeral { content = "Loading ..." }
            updateControls(myTurn)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DiscordUnoPlayer) return false

        if (owner != other.owner) return false

        return true
    }

    override fun hashCode(): Int {
        return owner.hashCode()
    }
}
