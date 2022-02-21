package dev.schlaubi.mikbot.util_plugins.leaderboard.gdpr

import dev.kord.core.entity.User
import dev.schlaubi.mikbot.core.gdpr.api.PermanentlyStoredDataPoint
import dev.schlaubi.mikbot.util_plugins.leaderboard.LeaderBoardDatabase
import dev.schlaubi.mikbot.util_plugins.leaderboard.LeaderBoardEntry
import org.litote.kmongo.eq

object LeaderBoardDataPoint : PermanentlyStoredDataPoint(
    "leaderboard",
    "gdpr.name",
    "gdpr.description"
) {
    override suspend fun deleteFor(user: User) {
        LeaderBoardDatabase.leaderboardEntries.deleteMany(
            LeaderBoardEntry::userId eq user.id
        )
    }

    override suspend fun requestFor(user: User): List<String> = LeaderBoardDatabase.leaderboardEntries.find(
        LeaderBoardEntry::userId eq user.id
    ).toList().map {
        "Guild: ${it.guildId}, Level: ${it.level}, Points: ${it.points}"
    }
}
