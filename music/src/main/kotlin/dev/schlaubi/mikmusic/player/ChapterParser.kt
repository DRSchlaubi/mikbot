package dev.schlaubi.mikmusic.player

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.time.Duration

// https://regex101.com/r/9gDHnl/9
private val normalPattern = """(?<time>(?:[0-9]+(?::|\s)){2,})[\s-]*(?<title>.*)""".toRegex()

// https://regex101.com/r/7G0kx4/3
private val appendingPattern = """(?<title>[0-9]+\..*(?= [0-9])\s)(?<time>(?:[0-9]+(?::|\s|${'$'}))+)""".toRegex()
private const val timeSeparator = ':'

fun String.parseChapters(): List<Chapter>? {
    val appendingFormat = appendingPattern.findAll(this).toList()
    val matches = appendingFormat.ifEmpty {
        normalPattern.findAll(this).toList()
    }
    val chapters = matches.map {
        val time = it.groups["time"]!!.value
        val title = it.groups["title"]!!.value

        val startTime = time.parseDuration()

        Chapter(startTime, title)
    }

    return chapters.ifEmpty {
        return null
    }
}

private fun String.parseDuration(): Duration {
    val units = split(timeSeparator)
    val unitCount = units.size - 1
    val multiplierOffset = if (unitCount > 2) 1 else 0

    val seconds = units.foldRightIndexed(0) { index, input, acc ->
        val multiplier = 60.0.pow(multiplierOffset + (unitCount - index)).toInt()
        val parsed = input.trimEnd().toInt() * multiplier

        acc + parsed
    }

    return Duration.seconds(seconds)
}

@Serializable

data class Chapter(
    @Contextual
    val startTime: Duration,
    val title: String
)
