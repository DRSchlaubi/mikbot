package dev.schlaubi.mikbot.game.multiple_choice.mechanics

import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.game.multiple_choice.player.MultipleChoicePlayer

/**
 * Default implementation of [GameMechanics] using [GameMechanics.NO_HINTS] and [DefaultPointsDistributor].
 *
 * @see GameMechanics
 */
class DefaultGameMechanics<Player : MultipleChoicePlayer> : GameMechanics<Player> {
    override val pointsDistributor: PointsDistributor<Player> = DefaultPointsDistributor()
}

/**
 * Default implementation of [PointsDistributor] awarding 1 point per turn.
 *
 * @see PointsDistributor
 */
class DefaultPointsDistributor<Player : MultipleChoicePlayer> : PointsDistributor<Player> {
    private val points = mutableMapOf<Snowflake, Int>()
    override fun awardPoints(player: Player) {
        val current = points[player.user.id] ?: 0
        points[player.user.id] = current + 1
    }

    override fun removePoints(player: Player) = Unit

    override fun List<Player>.sortByRank(): List<Player> = sortedBy {
        points[it.user.id] ?: 0
    }
}
