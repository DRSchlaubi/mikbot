package dev.schlaubi.mikbot.game.music_quiz.game

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.math.round
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Suppress("DataClassCanBeRecord")
data class Statistics(
    val points: Int,
    val responseTimes: List<Duration>,
    val gameSize: Int
) : Comparable<Statistics> {
    val average: Duration by lazy {
        val average = round(responseTimes.map { it.inWholeMilliseconds }.average())
        average.milliseconds
    }

    override fun compareTo(other: Statistics): Int {
        val pointsComparison = points.compareTo(other.points)
        if (pointsComparison != 0) {
            return pointsComparison
        }

        return average.compareTo(other.average)
    }
}

fun SongQuizGame.addStats(id: Snowflake, startTime: Instant, scored: Boolean) {
    val stats = gameStats[id] ?: Statistics(0, emptyList(), quizSize)

    val newStats = stats.copy(
        points = if (scored) stats.points + 1 else stats.points,
        responseTimes = stats.responseTimes + (Clock.System.now() - startTime)
    )

    gameStats[id] = newStats
}
