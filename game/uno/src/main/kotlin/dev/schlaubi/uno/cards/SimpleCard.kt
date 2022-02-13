package dev.schlaubi.uno.cards

import dev.schlaubi.uno.UnoColor

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
        return (card as? SimpleCard)?.number == number
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

/**
 * A simple uno card.
 */
@Suppress("DataClassCanBeRecord")
public data class SimpleCard(override val number: Int, override val color: UnoColor) : NumberedCard()
