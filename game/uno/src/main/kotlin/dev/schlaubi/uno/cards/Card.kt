package dev.schlaubi.uno.cards

import dev.schlaubi.uno.Game
import dev.schlaubi.uno.Player
import dev.schlaubi.uno.UnoColor

/**
 * Representation of a normal Uno card.
 */
public sealed interface Card : Comparable<Card> {
    /**
     * Checks whether this card can be played on [the other card][card].
     */
    public fun canBePlayedOn(card: PlayedCard): Boolean

    /**
     * Convert's this card into a [PlayedCard].
     *
     * @param color the [UnoColor] chosen if this is a [WildCard]
     *
     * @see ColoredCard
     */
    public fun play(color: UnoColor): PlayedCard
}

/**
 * A [Card] with a special action (like skip, reverse)
 */
public sealed interface ActionCard : Card {
    /**
     * Applies this cards action to [game].
     *
     * @param player the [Player] who played this card.
     */
    public suspend fun applyToGame(game: Game<*>, player: Player)
}

/**
 * A card which has already been played.
 */
public sealed interface PlayedCard : Card {
    /**
     * This card's fixed color (See [ColoredCard]) or the color chosen if this is a wild card.
     */
    public val color: UnoColor
}

/**
 * A card causing the next player to draw cards.
 *
 * @property cards the number of cards to draw
 */
public sealed interface DrawingCard : Card {
    public val cards: Int
    /**
     * Checks whether this card can stack with [card] or not.
     */
    public fun canStackWith(card: PlayedCard): Boolean

}

/**
 * Abstract implementation of [Card] and [PlayedCard] for cards which have a fixed color.
 */
public sealed class ColoredCard : PlayedCard {
    override fun canBePlayedOn(card: PlayedCard): Boolean = card.color == color

    final override fun play(color: UnoColor): PlayedCard = this

    override fun compareTo(other: Card): Int {
        return if (other is PlayedCard) {
            color.compareTo(other.color)
        } else {
            0
        }
    }
}

/**
 * Abstract implementation of colored cards with numbers on them.
 *
 * @property number the number of this card
 *
 * @see ColoredCard
 * @see SimpleCard
 */
public abstract class NumberedCard : ColoredCard() {
    public abstract val number: Int

    override fun canBePlayedOn(card: PlayedCard): Boolean {
        if (super.canBePlayedOn(card)) return true // color check
        return (card as? NumberedCard)?.number == number
    }

    override fun compareTo(other: Card): Int {
        val parent = super.compareTo(other)
        return if (parent == 0) {
            if (other is SimpleCard) {
                number.compareTo(other.number)
            } else {
                -1
            }
        } else {
            parent
        }
    }
}
