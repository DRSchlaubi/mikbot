package dev.schlaubi.uno.cards

import dev.schlaubi.uno.Game
import dev.schlaubi.uno.Player
import dev.schlaubi.uno.UnoColor

/**
 * Similar to a [simple 7][SimpleCard] but with an [action][ActionCard] causing the player to switch cards with [switchWith].
 */
public class CardSwitching7(private val switchWith: Player, override val color: UnoColor) : NumberedCard(), ActionCard {
    override val number: Int = 7

    override suspend fun applyToGame(game: Game<*>, player: Player) {
        val temp = switchWith.deck.toMutableList()
        switchWith.deck = (player.deck - this).toMutableList()
        switchWith.refreshCards()
        player.deck = temp
    }
}

/**
 * Similar to a [simple 0][SimpleCard] but with an [action][ActionCard] causing all players to switch cards with their neighbours.
 *
 * @see Game.orderedPlayers
 */
public class CardRotating0(override val color: UnoColor) : NumberedCard(), ActionCard {
    override val number: Int = 0
    override suspend fun applyToGame(game: Game<*>, player: Player) {
        val players = game.orderedPlayers.zipWithNext()

        val toSwitchDecks = players.map { (switchWith, original) ->
            original to (if (switchWith == player) (player.deck - this) else switchWith.deck).toMutableList()
        }

        toSwitchDecks.forEach { (toSwitchPlayer, deck) ->
            toSwitchPlayer.deck = deck
            if (toSwitchPlayer != player) {
                toSwitchPlayer.refreshCards()
            }
        }
    }
}
