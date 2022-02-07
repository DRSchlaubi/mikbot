package dev.schlaubi.mikbot.game.uno.game.player

import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.interaction.*
import dev.kord.core.entity.interaction.FollowupMessage
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.schlaubi.mikbot.game.api.ControlledPlayer
import dev.schlaubi.mikbot.game.uno.game.DiscordUnoGame
import dev.schlaubi.mikbot.game.uno.game.ui.translationKey
import dev.schlaubi.mikbot.plugin.api.util.deleteAfterwards
import dev.schlaubi.uno.Game
import dev.schlaubi.uno.Player
import dev.schlaubi.uno.cards.AbstractWildCard
import dev.schlaubi.uno.cards.DiscardAllCardsCard
import dev.schlaubi.uno.cards.SlapCard
import dev.schlaubi.uno.cards.SlapContext
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import dev.schlaubi.mikbot.game.api.Player as GamePlayer

internal val unoInteractionTimeout = 30.seconds.inWholeMilliseconds

const val drawCardButton = "draw_card"
const val sayUnoButton = "say_uno"
const val skipButton = "skip"
const val allCardsButton = "request_all_cards"

class DiscordUnoPlayer(
    override val user: UserBehavior,
    val response: EphemeralInteractionResponseBehavior,
    override var controls: FollowupMessage,
    val game: DiscordUnoGame
) : Player(), GamePlayer, ControlledPlayer {
    override val ack: InteractionResponseBehavior
        get() = response

    private var myTurn = false
    internal var drawn = false
    var turns: Int = 0
        private set

    override fun onSkip() {
        if (game.flashMode) {
            game.launch {
                response.followUpEphemeral {
                    content = translate("uno.flash.skipped")
                }
            }
        }
    }

    override fun onWin(place: Int) {
        game.kord.launch {
            game.thread.createMessage("${user.mention} finished the game!")
            controls.edit {
                components = mutableListOf()
                content = translate("uno.controls.won")
            }
        }
    }

    override fun forgotUno(game: Game<*>) {
        this.game.kord.launch {
            response.followUpEphemeral {
                content = translate("uno.general.forgot_uno")
            }
        }
    }

    suspend fun turn() {
        myTurn = true
        updateControls(true)
        val cantPlay by lazy { deck.none { it.canBePlayedOn(game.game.topCard) } }
        if (drawn) {
            if (cantPlay) {
                return endTurn() // auto-skip if drawn and can't play
            }
        } else if (game.game.drawCardSum >= 1 && cantPlay) {
            doDraw() // auto draw if there is no other option
            return turn()
        }

        val cardName = awaitResponse { controls }

        myTurn = false // Prevent clicking again
        if (cardName != null) {
            if (normalPlay(cardName)) { // if the action calls for a new turn repeat
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
        turns++
    }

    private suspend fun normalPlay(cardName: String): Boolean {
        @Suppress("DUPLICATE_LABEL_IN_WHEN") // not duplicated, we want to purposely not skip
        when (cardName) {
            drawCardButton -> {
                doDraw()
                return true
            }
            sayUnoButton -> {
                playUno()
                return true
            }
            allCardsButton -> {
                val cards = deck.map { translate(it.translationKey) }.joinToString(", ")
                response.followUpEphemeral {
                    content = cards.substring(0, 2000.coerceAtMost(cards.length))
                }
                return true
            }
            skipButton -> return false
            else -> {
                val cardId = cardName.substringAfter("play_card_").toInt()
                val card = deck[cardId]
                if (card is DiscardAllCardsCard) {
                    uno() // you cannot say uno unless having two cards, so we just auto-uno here
                }

                if (card is SlapCard) {
                    updateControls(false) // Disable controls for player during slap turn
                }

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

    private fun doDraw() {
        drawn = true
        draw(game.game)
    }

    private suspend fun playUno() {
        uno()
        val message = game.thread.createMessage("${user.mention} just said UNO!")

        game.kord.launch {
            runCatching {
                // Ignore thread is archived errors
                message.deleteAfterwards(15.seconds)
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

    override suspend fun resendControls(ack: EphemeralInteractionResponseBehavior) = resendControlsInternally(null)

    suspend fun resendControlsInternally(
        event: ComponentInteractionCreateEvent?,
        justLoading: Boolean = false
    ) {
        val ack = event?.interaction?.acknowledgeEphemeral() ?: response
        controls = ack.followUpEphemeral {
            content = translate("uno.controls.loading")
        }
        if (!justLoading) {
            updateControls(myTurn)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DiscordUnoPlayer) return false

        if (user != other.user) return false

        return true
    }

    override fun hashCode(): Int {
        return user.hashCode()
    }

    override fun onSlap(context: SlapContext) {
        game.launch {
            openSlapCardUI(context)
        }
    }

    override fun onSlapEnd() {
        game.launch {
            updateControls(false)
            response.followUp {
                translate("game.slap_session.lost")
            }
        }
    }

    override fun toString(): String = user.toString()
}
