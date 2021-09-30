package dev.schlaubi.uno.cards

import dev.schlaubi.uno.UnoColor

/**
 * Abstract implementation of all wild cards.
 *
 * @see WildCard
 */
public sealed class AbstractWildCard : Card {
    override fun canBePlayedOn(card: PlayedCard): Boolean = true // wild cards work on every other card
    override fun play(color: UnoColor): PlayedCard = PlayedWildCard(color)
}

/**
 * Implementation of a non-played wild card
 *
 * @see PlayedWildCard
 * @see WildCardDraw4
 */
public class WildCard : AbstractWildCard() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }
}

/**
 * Extension of [WildCard] also implementing [DrawingCard] to draw 4 cards.
 *
 * @see WildCard
 * @see PlayedWildCard
 */
public class WildCardDraw4 : AbstractWildCard(), DrawingCard {
    override val cards: Int = 4

    override fun play(color: UnoColor): PlayedCard = PlayedWildCardDraw4(super.play(color))

    private inner class PlayedWildCardDraw4(val delegate: PlayedCard) : AbstractWildCard(), PlayedCard by delegate,
        DrawingCard {
        override val cards: Int = this@WildCardDraw4.cards
        override fun play(color: UnoColor): PlayedCard = delegate.play(color)

        override fun canBePlayedOn(card: PlayedCard): Boolean = delegate.canBePlayedOn(card)
    }
}

/**
 * Implementation of [PlayedCard] for all wild cards.
 */
public class PlayedWildCard(override val color: UnoColor) : AbstractWildCard(), PlayedCard
