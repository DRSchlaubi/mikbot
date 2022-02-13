package dev.schlaubi.uno.cards

import dev.schlaubi.uno.UnoColor

/**
 * A simple uno card.
 */
@Suppress("DataClassCanBeRecord")
public data class SimpleCard(override val number: Int, override val color: UnoColor) : NumberedCard()
