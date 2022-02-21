package dev.schlaubi.mikbot.util_plugins.leaderboard.core

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.getChannelOfOrNull
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.schlaubi.mikbot.util_plugins.leaderboard.LeaderBoardDatabase
import dev.schlaubi.mikbot.util_plugins.leaderboard.calculateXPForNextLevel
import dev.schlaubi.mikbot.util_plugins.leaderboard.findByMember
import kotlinx.datetime.Clock
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

suspend fun LeaderBoardModule.leaderBoardExecutor() = event<MessageCreateEvent> {
    check {
        anyGuild()
        isNotBot()
    }

    action {
        val member = event.message.getAuthorAsMember() ?: return@action
        val existingUser = LeaderBoardDatabase.leaderboardEntries.findByMember(member)

        if (Clock.System.now() - existingUser.lastXpReceived <= 1.minutes) return@action

        val xpGain = Random.nextInt(15, 25)

        val user = if (existingUser.points + xpGain >= calculateXPForNextLevel(existingUser.level)) {
            val settings = LeaderBoardDatabase.settings.findOneById(member.guildId)
            if (settings?.levelUpChannel != null) {
                val channel = event.getGuild()!!.getChannelOfOrNull<GuildMessageChannel>(settings.levelUpChannel)
                if (channel != null) {
                    channel.createMessage(
                        settings.levelUpMessage
                            .replace("%mention%", member.mention)
                            .replace("%level%", (existingUser.level + 1).toString())
                    )
                } else {
                    LeaderBoardDatabase.settings.save(settings.copy(levelUpChannel = null))
                }
            }
            existingUser.copy(
                points = 0,
                level = existingUser.level + 1,
                lastXpReceived = Clock.System.now()
            )
        } else {
            existingUser.copy(points = existingUser.points + xpGain, lastXpReceived = Clock.System.now())
        }

        LeaderBoardDatabase.leaderboardEntries.save(user)
    }
}
