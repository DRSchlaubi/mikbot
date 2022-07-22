package dev.schlaubi.mikbot.game.multiple_choice.mechanics

import dev.schlaubi.mikbot.game.multiple_choice.player.MultipleChoicePlayer
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DefaultStreakGameMechanics<Player : MultipleChoicePlayer> : StreakBasedGameMechanics<Player>() {
    override val showAnswersAfter: Duration = 5.seconds

    override fun calculatePoints(timeSinceAnswer: Duration): Int {
        val seconds = timeSinceAnswer.inWholeMilliseconds / 1000.0
        // https://www.geogebra.org/m/xvxfuemb
        val exactResult = 981.316 * exp(-0.173 * seconds) + 100.0
        return exactResult.roundToInt()
    }

    // each streak gives you a 10 percent bonus
    override fun calculateStreakMultiplier(streak: Int): Double {
        val bonus = (0.1 * streak) - 0.1 // -0.1=steak(1)=0
        return (1 + bonus).coerceAtLeast(1.0)
    }
}
