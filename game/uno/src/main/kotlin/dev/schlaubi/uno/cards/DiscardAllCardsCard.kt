package dev.schlaubi.uno.cards

import dev.schlaubi.uno.Game
import dev.schlaubi.uno.Player
import dev.schlaubi.uno.UnoColor

/**
 * A card for [Game.extreme] allowing you to discard all cards colored with [color] at once.
 *
 * @see Game.extreme
 */
public class DiscardAllCardsCard(override val color: UnoColor) : ColoredCard(), ActionCard {
    override fun canBePlayedOn(card: PlayedCard): Boolean = super.canBePlayedOn(card)
            || card is DiscardAllCardsCard

    override suspend fun applyToGame(game: Game<*>, player: Player) {
        val cards = player.deck.filter { (it as? ColoredCard)?.color == color }
        player.deck.removeAll(cards)
        game.playedDeck.addAll(cards.map { it.play(color) })
    }
}
