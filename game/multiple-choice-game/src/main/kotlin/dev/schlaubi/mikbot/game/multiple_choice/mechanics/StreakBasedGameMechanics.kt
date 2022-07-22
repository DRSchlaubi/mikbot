package dev.schlaubi.mikbot.game.multiple_choice.mechanics

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.schlaubi.mikbot.game.multiple_choice.AnswerContext
import dev.schlaubi.mikbot.game.multiple_choice.player.MultipleChoicePlayer
import dev.schlaubi.mikbot.game.multiple_choice.translateInternally
import kotlin.time.Duration

/**
 * Abstract implementation of [GameMechanics] with support for Streaks.
 */
abstract class StreakBasedGameMechanics<Player : MultipleChoicePlayer> : GameMechanics<Player> {
    final override val pointsDistributor: PointsDistributor<Player> = PointsDistributorImpl()

    /**
     * Calculates the points for a user, who answered correctly after [timeSinceAnswer] of time.
     */
    open fun calculatePoints(timeSinceAnswer: Duration): Int = 1

    /**
     * Calculates the streak multiplier for [streak].
     */
    open fun calculateStreakMultiplier(streak: Int): Double = 1.0

    private inner class PointsDistributorImpl : PointsDistributor<Player> {
        private val points = mutableMapOf<Snowflake, Int>()
        private val streaks = mutableMapOf<Snowflake, Int>()

        override fun retrievePointsForPlayer(player: Player): Int = points[player.user.id] ?: 0

        override fun awardPoints(player: Player, timeSinceAnswer: Duration): Int {
            val streak = (streaks[player.user.id] ?: 0) + 1
            streaks[player.user.id] = streak

            val points = (calculatePoints(timeSinceAnswer) * calculateStreakMultiplier(streak)).toInt()
            val currentPoints = this.points[player.user.id] ?: 0
            this.points[player.user.id] = currentPoints + points
            return points
        }

        override fun removePoints(player: Player): Int {
            streaks[player.user.id] = 0
            return 0
        }

        override fun List<Player>.sortByRank(): List<Player> = sortedByDescending {
            points[it.user.id] ?: 0
        }

        override suspend fun sendPointMessage(user: UserBehavior, answer: AnswerContext) {
            val streak = streaks[user.id] ?: 0
            val multiplier = calculateStreakMultiplier(streak)
            answer.response.createEphemeralFollowup {
                content = answer.game.translateInternally(
                    answer.interactionCreateEvent,
                    "multiple_choice_game.received_points",
                    answer.points,
                    streak,
                    multiplier
                )
            }
        }
    }
}
