package dev.schlaubi.uno.cards

import dev.schlaubi.uno.Game
import dev.schlaubi.uno.Player
import dev.schlaubi.uno.UnoColor
import kotlinx.coroutines.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * The Slap card from the variant UNO Flash.
 */
public class SlapCard(override val color: UnoColor) : ColoredCard(), ActionCard {
    override fun canBePlayedOn(card: PlayedCard): Boolean = super.canBePlayedOn(card) || card is SlapCard

    @OptIn(ExperimentalTime::class)
    override suspend fun applyToGame(game: Game<*>, player: Player) {
        if (game.players.size > 2) {
            val safePlayers = mutableListOf<Player>()
            withTimeoutOrNull(30.seconds) {
                suspendCoroutine<List<Player>> { cont ->
                    val context = SlapContext(game, player, cont, safePlayers)
                    (game.players - player).forEach {
                        it.onSlap(context)
                    }
                }
            }
        }
    }
}

/**
 * Context if a [SlapCard] is played.
 */
public class SlapContext internal constructor(
    private val game: Game<out Player>,
    private val initiator: Player,
    private val continuation: Continuation<List<Player>>,
    playersDelegate: MutableList<Player>
) {
    private val players = ObservableList(playersDelegate, ::observeAdd)

    @Suppress("UNUSED_PARAMETER") // Required for signature
    private fun observeAdd(player: Player) {
        if (players.size >= game.players.size - 2) {
            val remainingPlayer = (game.players - players - initiator).first()
            remainingPlayer.onSlapEnd()
            game.drawCards(remainingPlayer, 2)

            continuation.resume(players)
        }
    }

    /**
     * Makes [player] slap the button.
     */
    public fun slap(player: Player) {
        players += player
    }
}

private class ObservableList<T>(private val delegate: MutableList<T>, private val observer: (T) -> Unit) :
    MutableList<T> by delegate {
    override fun add(element: T): Boolean = delegate.add(element).also {
        observer(element)
    }
}
