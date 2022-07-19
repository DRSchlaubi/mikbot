package dev.schlaubi.mikbot.game.multiple_choice.mechanics

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
     * Awards points after a player answered correctly.
     * This should also store a possible streak (All answers are correct until [removePoinst] is called
     */
    fun awardPoints(player: Player)

    /**
     * Removes points after a player answered incoreectly
     */
    fun removePoints(player : Player)

    /**
     * How to rank [players][Player] at the end of a round.
     */
    fun List<Player>.sortByRank(): List<Player>
}
