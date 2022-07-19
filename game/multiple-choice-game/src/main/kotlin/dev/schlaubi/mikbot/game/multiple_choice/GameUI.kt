package dev.schlaubi.mikbot.game.multiple_choice

import dev.kord.core.behavior.UserBehavior
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis
import dev.schlaubi.mikbot.game.api.module.commands.formatPercentage
import dev.schlaubi.mikbot.game.multiple_choice.player.Statistics
import dev.schlaubi.mikbot.plugin.api.util.effectiveAvatar

data class AnswerPair(val answerIndex: Int?, val correct: Boolean) {
    val emoji: DiscordEmoji
        get() = when (answerIndex) {
            null -> Emojis.clock
            0 -> Emojis.one
            1 -> Emojis.two
            2 -> Emojis.three
            4 -> Emojis.four
            else -> error("Unexpected AnswerIndex")
        }
}

internal fun EmbedBuilder.addPlayers(players: Map<UserBehavior, AnswerPair>, showCorrect: Boolean = true) {
    field {
        name = "Answers"
        value = if (players.isNotEmpty()) {
            players.map { (player, answer) ->
                val checkEmoji: Any = when {
                    !showCorrect -> ""
                    answer.correct -> Emojis.whiteCheckMark
                    else -> Emojis.noEntrySign
                }

                "${player.mention} - ${answer.emoji} $checkEmoji"
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
