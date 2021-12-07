package dev.schlaubi.mikbot.game.api

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Game stats.
 *
 * @property wins the total amount of wins
 * @property losses the total amount of losses
 * @property ratio the win/lose ratio
 */

@Serializable
data class GameStats(
    val wins: Int,
    val losses: Int,
    val ratio: Double,
    val totalGamesPlayed: Int
)

@Serializable
data class UserGameStats(
    @SerialName("_id")
    val user: Snowflake,
    val stats: GameStats
)
