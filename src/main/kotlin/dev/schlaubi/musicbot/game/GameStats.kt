package dev.schlaubi.musicbot.game

import kotlinx.serialization.Serializable

/**
 * Game stats.
 *
 * @property wins the total amount of wins
 * @property losses the total amount of losses
 * @property ratio the win/lose ratio
 */
@JvmRecord
@Serializable
data class GameStats(
    val wins: Int,
    val losses: Int,
    val ratio: Double,
    val totalGamesPlayed: Int
)
