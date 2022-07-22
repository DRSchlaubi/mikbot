package dev.schlaubi.mikbot.game.multiple_choice.mechanics

import dev.kord.core.behavior.UserBehavior
import dev.schlaubi.mikbot.game.multiple_choice.AnswerContext
import dev.schlaubi.mikbot.game.multiple_choice.MultipleChoiceGame
import dev.schlaubi.mikbot.game.multiple_choice.player.MultipleChoicePlayer
import kotlin.time.Duration

/**
 * Interface for common mechanics of a [MultipleChoiceGame].
 *
 * @param Player the type of the games player
 * @property showAnswersAfter [Duration] after which the other players answers will be displayed
 * @property pointsDistributor [PointsDistributor] implementation used to distribute points after each turn
 */
interface GameMechanics<Player : MultipleChoicePlayer> {
    val showAnswersAfter: Duration
        get() = NO_HINTS

    val pointsDistributor: PointsDistributor<Player>

    companion object {
        /**
         * Constant to disable [showAnswersAfter] completely.
         */
        val NO_HINTS: Duration = Duration.ZERO
    }
}

/**
 * Creates a [GameMechanics] implementation with [pointsDistributor] and [showAnswersAfter].
 *
 * @see GameMechanics
 */
@Suppress("FunctionName")
fun <Player : MultipleChoicePlayer> GameMechanics(
    pointsDistributor: PointsDistributor<Player>,
    showAnswersAfter: Duration = GameMechanics.NO_HINTS,
): GameMechanics<Player> = GameMechanicsImpl(showAnswersAfter, pointsDistributor)

private class GameMechanicsImpl<Player : MultipleChoicePlayer>(
    override val showAnswersAfter: Duration,
    override val pointsDistributor: PointsDistributor<Player>,
) : GameMechanics<Player>

/**
 * Interface for point distribution after a multiple-choice turn.
 */
interface PointsDistributor<Player : MultipleChoicePlayer> {

    /**
     * Retrieves the points for [player].
     */
    fun retrievePointsForPlayer(player: Player): Int

    /**
     * Awards points after a player answered correctly.
     * This should also store a possible streak (All answers are correct until [removePoints] is called
     *
     * @return the points that have been awarded
     */
    fun awardPoints(player: Player, timeSinceAnswer: Duration): Int

    /**
     * Removes points after a player answered incorrectly
     *
     * @return the amount of points that have been removed
     */
    fun removePoints(player : Player): Int

    /**
     * How to rank [players][Player] at the end of a round.
     */
    fun List<Player>.sortByRank(): List<Player>

    /**
     * Optionally sends a message to [user] for the points rewarded for [answer].
     *
     * @see AnswerContext.response
     */
    suspend fun sendPointMessage(user: UserBehavior, answer: AnswerContext) = Unit
}
