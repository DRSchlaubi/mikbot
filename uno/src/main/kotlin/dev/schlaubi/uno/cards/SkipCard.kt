package dev.schlaubi.uno.cards

import dev.schlaubi.uno.Game
import dev.schlaubi.uno.UnoColor

/**
 * Implementation of [Card], skipping the next player.
 *
 * @see ColoredCard
 * @see ActionCard
 */
@Suppress("DataClassCanBeRecord")
public data class SkipCard(override val color: UnoColor) : ColoredCard(), ActionCard {
    override fun canBePlayedOn(card: PlayedCard): Boolean = super.canBePlayedOn(card) || card is SkipCard
    override fun applyToGame(game: Game<*>) {
        game.skipPlayer()
    }
}
