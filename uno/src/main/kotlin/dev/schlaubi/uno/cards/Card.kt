package dev.schlaubi.uno.cards

import dev.schlaubi.uno.Game
import dev.schlaubi.uno.UnoColor

/**
 * Representation of a normal Uno card.
 */
public sealed interface Card {
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
     */
    public fun applyToGame(game: Game<*>)
}

/**
 * Abstract implementation of [Card] and [PlayedCard] for cards which have a fixed color.
 */
public sealed class ColoredCard : PlayedCard {
    override fun canBePlayedOn(card: PlayedCard): Boolean = card.color == color

    final override fun play(color: UnoColor): PlayedCard = this
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
}
