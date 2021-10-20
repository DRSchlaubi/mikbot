package dev.schlaubi.musicbot.module.uno.game.player

import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.entity.interaction.InteractionFollowup
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.schlaubi.musicbot.game.confirmation
import dev.schlaubi.musicbot.module.uno.game.DiscordUnoGame
import dev.schlaubi.musicbot.module.uno.game.ui.translationKey
import dev.schlaubi.musicbot.utils.deleteAfterwards
import dev.schlaubi.uno.Game
import dev.schlaubi.uno.Player
import dev.schlaubi.uno.cards.AbstractWildCard
import dev.schlaubi.uno.cards.DiscardAllCardsCard
import kotlinx.coroutines.launch
import kotlin.time.Duration
import dev.schlaubi.musicbot.game.Player as GamePlayer

internal val unoInteractionTimeout = Duration.seconds(30).inWholeMilliseconds

const val drawCardButton = "draw_card"
const val sayUnoButton = "say_uno"
const val skipButton = "skip"
const val allCardsButton = "request_all_cards"

class DiscordUnoPlayer(
    override val user: UserBehavior,
    val response: EphemeralInteractionResponseBehavior,
    var controls: InteractionFollowup,
    val game: DiscordUnoGame
) : Player(), GamePlayer {
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

    suspend fun resendControls(
        event: ComponentInteractionCreateEvent?,
        justLoading: Boolean = false,
        overrideConfirm: Boolean = false
    ) {
        val ack = event?.interaction?.acknowledgeEphemeral() ?: response
        val confirmed = overrideConfirm || game.confirmation(ack) {
            content = translate("uno.resend_controls.confirm")
        }.value
        if (confirmed) {
            controls.edit {
                content = translate("uno.controls.reset")
                components = mutableListOf()
            }
            if (!overrideConfirm) {
                game.thread.createMessage {
                    content = translate("uno.resend_controls.blame", user.mention)
                }.pin()
            }

            controls = ack.followUpEphemeral {
                content = translate("uno.controls.loading")
            }
            if (!justLoading) {
                updateControls(myTurn)
            }
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
}
