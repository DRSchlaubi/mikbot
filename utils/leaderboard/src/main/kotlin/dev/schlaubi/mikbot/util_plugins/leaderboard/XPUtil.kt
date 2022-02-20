package dev.schlaubi.mikbot.util_plugins.leaderboard

import kotlin.math.pow

fun calculateXPNeededForNextLevel(level: Int) = 5 * (level.toDouble().pow(2)).toLong() + 50 * level.toLong() + 100

fun calculateXpForLevel(level: Int) = (1..level).fold(0L) { acc, i ->
    acc + calculateXPNeededForNextLevel(i)
}

fun calculateXPForNextLevel(level: Int) = calculateXpForLevel(level + 1)
