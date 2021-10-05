package dev.schlaubi.uno.cards

import dev.schlaubi.uno.UnoColor

/**
 * A simple uno card.
 *
 * @property number the number of this card
 *
 * @see ColoredCard
 */
@Suppress("DataClassCanBeRecord")
public data class SimpleCard(val number: Int, override val color: UnoColor) : ColoredCard() {
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
