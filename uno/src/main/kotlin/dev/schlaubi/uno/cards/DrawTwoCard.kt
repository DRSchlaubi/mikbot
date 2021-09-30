package dev.schlaubi.uno.cards

import dev.schlaubi.uno.UnoColor

/**
 * A [ColoredCard] making the next player draw 2 cards.
 *
 * @see DrawingCard
 */
@Suppress("DataClassCanBeRecord")
public data class DrawTwoCard(override val color: UnoColor) : ColoredCard(), DrawingCard {
    override fun canBePlayedOn(card: PlayedCard): Boolean = super.canBePlayedOn(card) || card is DrawTwoCard
    override val cards: Int = 2
}
