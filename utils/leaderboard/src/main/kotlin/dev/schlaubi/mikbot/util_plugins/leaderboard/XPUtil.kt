package dev.schlaubi.mikbot.util_plugins.leaderboard

import kotlin.math.pow

fun calculateXPForNextLevel(level: Int) = 5 * (level.toDouble().pow(2)).toLong() + 50 * level.toLong() + 100
