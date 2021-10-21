package dev.schlaubi.musicbot.module.music.player

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.time.Duration

// https://regex101.com/r/9gDHnl/5
private val chapterPattern = """((?:[0-9]+(?::|\s))+)\s*(.*)""".toRegex()
private const val timeSeparator = ':'

fun String.parseChapters(): List<Chapter>? {
    val matches = chapterPattern.findAll(this)
    val chapters = matches.map {
        val (time, title) = it.destructured
        val startTime = time.parseDuration()

        Chapter(startTime, title)
    }
        .toList()

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
@JvmRecord
data class Chapter(
    @Contextual
    val startTime: Duration,
    val title: String
)
