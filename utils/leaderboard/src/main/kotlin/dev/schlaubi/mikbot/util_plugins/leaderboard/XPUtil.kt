package dev.schlaubi.mikbot.util_plugins.leaderboard

fun calculateXPForNextLevel(level: Int) = 5 * (level.toLong() xor 2) + 50 * level.toLong() + 100
