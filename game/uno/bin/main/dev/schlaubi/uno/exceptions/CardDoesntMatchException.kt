package dev.schlaubi.uno.exceptions

import dev.schlaubi.uno.Game
import dev.schlaubi.uno.cards.PlayedCard

/**
 * Exception thrown when a player tries to play a [Card][PlayedCard], which doesn't match the top [Card][PlayedCard]
 *
 * @property currentCard the current top card
 * @property triedCard the card the player tried to play
 *
 * @see Game.topCard
 */
public class CardDoesntMatchException(public val currentCard: PlayedCard, public val triedCard: PlayedCard) :
    RuntimeException()
