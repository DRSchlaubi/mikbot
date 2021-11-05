package dev.schlaubi.uno.exceptions

import dev.schlaubi.uno.cards.Card

/**
 * Exception thrown when a player tries to play a [Card] he doesn't have.
 *
 * @property card the [Card] the player tried to play
 */
public class PlayerDoesNotHaveCardException(public val card: Card) : RuntimeException()
