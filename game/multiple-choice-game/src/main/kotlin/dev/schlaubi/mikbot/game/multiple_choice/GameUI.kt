package dev.schlaubi.mikbot.game.multiple_choice

import dev.kord.core.behavior.UserBehavior
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.emoji.Emojis
import dev.schlaubi.mikbot.game.api.module.commands.formatPercentage
import dev.schlaubi.mikbot.game.multiple_choice.player.Statistics
import dev.schlaubi.mikbot.plugin.api.util.effectiveAvatar

internal fun EmbedBuilder.addPlayers(players: Map<UserBehavior, Boolean>) {
    field {
        name = "Answers"
        value = if (players.isNotEmpty()) {
            players.map { (player, wasCorrect) ->
                val emoji = if (wasCorrect) Emojis.whiteCheckMark else Emojis.noEntrySign

                "${player.mention} - $emoji"
            }.joinToString("\n")
        } else {
            "No one answered :("
        }
    }
}

internal suspend fun EmbedBuilder.addUserStats(userBehavior: UserBehavior, stats: Statistics) {
    author {
        val user = userBehavior.asUser()
        name = user.username
        icon = user.effectiveAvatar
    }

    field {
        name = "Total points"
        value =
            "${stats.points}/${stats.gameSize} (${(stats.points.toDouble() / stats.gameSize.toDouble()).formatPercentage()})"
    }

    field {
        name = "Average response time"
        value = stats.average.toString()
    }
}

internal suspend fun EmbedBuilder.addGameEndEmbed(game: MultipleChoiceGame<*, *, *>) {
    val user = game.wonPlayers.firstOrNull()?.user ?: return
    addUserStats(user, game.gameStats[user.id] ?: Statistics(0, emptyList(), game.quizSize))
}
