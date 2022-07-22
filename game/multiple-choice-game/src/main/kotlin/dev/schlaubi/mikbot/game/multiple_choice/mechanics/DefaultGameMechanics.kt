package dev.schlaubi.mikbot.game.multiple_choice.mechanics

import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.game.multiple_choice.player.MultipleChoicePlayer
import dev.schlaubi.mikbot.game.multiple_choice.player.Statistics
import kotlin.time.Duration

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
    private val responseTimes = mutableMapOf<Snowflake, List<Duration>>()

    override fun retrievePointsForPlayer(player: Player): Int = points[player.user.id] ?: 0

    override fun awardPoints(player: Player, timeSinceAnswer: Duration): Int {
        val current = points[player.user.id] ?: 0
        val newList = responseTimes[player.user.id]?.let { it + timeSinceAnswer } ?: listOf(timeSinceAnswer)
        responseTimes[player.user.id] = newList
        points[player.user.id] = current + 1
        return 1
    }

    override fun removePoints(player: Player) = 0

    override fun List<Player>.sortByRank(): List<Player> = sortedByDescending {
        Statistics(points[it.user.id] ?: 0, responseTimes[it.user.id] ?: emptyList(), 0)
    }
}
