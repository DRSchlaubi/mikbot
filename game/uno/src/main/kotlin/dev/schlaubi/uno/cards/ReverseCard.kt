package dev.schlaubi.uno.cards

import dev.schlaubi.uno.Game
import dev.schlaubi.uno.Player
import dev.schlaubi.uno.UnoColor

/**
 * Implementation of [Card], reversing [Game.direction].
 *
 * @see ColoredCard
 * @see ActionCard
 */
@Suppress("DataClassCanBeRecord")
public data class ReverseCard(override val color: UnoColor) : ColoredCard(), ActionCard {
    override fun canBePlayedOn(card: PlayedCard): Boolean = super.canBePlayedOn(card) || card is ReverseCard
    override suspend fun applyToGame(game: Game<*>, player: Player): Unit = game.reverse()
}
