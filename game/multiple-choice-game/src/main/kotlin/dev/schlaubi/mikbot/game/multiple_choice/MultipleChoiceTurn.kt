package dev.schlaubi.mikbot.game.multiple_choice

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import dev.schlaubi.mikbot.game.multiple_choice.mechanics.GameMechanics
import dev.schlaubi.mikbot.game.multiple_choice.player.MultipleChoicePlayer
import dev.schlaubi.mikbot.game.multiple_choice.player.addStats
import dev.schlaubi.mikbot.plugin.api.util.componentLive
import dev.schlaubi.mikbot.plugin.api.util.getLocale
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

internal suspend fun <Player : MultipleChoicePlayer, Q : Question> MultipleChoiceGame<Player, Q, *>.turn(question: Q) {
    val allAnswers = question.allAnswers.filter(String::isNotBlank)

    val turnStart = Clock.System.now()
    val message = thread.createMessage {
        embed {
            addQuestion(question, true)
        }
    }
    val uiMessage = thread.createMessage {
        content = EmbedBuilder.ZERO_WIDTH_SPACE
        actionRow {
            allAnswers.forEachIndexed { index, name ->
                interactionButton(ButtonStyle.Secondary, "choose_$index") {
                    label = (name as String?)?.take(80)
                }
            }
        }
        questionUI(question)
    }

    val answers = mutableMapOf<UserBehavior, AnswerPair>()

    // coroutineScope suspends until all child coroutines are dead
    // That way we can cancel all children at once
    val start = TimeSource.Monotonic.markNow()
    coroutineScope {
        lateinit var job: Job
        fun endTurn() = job.cancel()

        job = launch {
            val liveMessage = uiMessage.componentLive()
            launch { // this blocks this scope until we cancel it
                delay(30.seconds)
                endTurn()
            }

            if (mechanics.showAnswersAfter != GameMechanics.NO_HINTS) {
                launch {
                    delay(mechanics.showAnswersAfter)
                }
            }

            liveMessage.onInteraction {
                if (handle(question)) return@onInteraction // custom event handler

                val user = interaction.user
                val player = interaction.gamePlayer
                if (player == null) {
                    interaction.respondEphemeral {
                        content = translateInternally(user, "multiple_choice.game.not_in_game")
                    }
                    return@onInteraction
                }
                if (answers.containsKey(user)) {
                    interaction.respondEphemeral {
                        content = translateInternally(user, "multiple_choice.game.already_submitted")
                    }
                    return@onInteraction
                }
                interaction.deferEphemeralMessageUpdate()
                val index = interaction.componentId.substringAfter("choose_").toInt()
                val name = allAnswers[index]
                val wasCorrect = name == question.correctAnswer
                if (wasCorrect) {
                    mechanics.pointsDistributor.awardPoints(player)
                } else {
                    mechanics.pointsDistributor.removePoints(player)
                }
                answers[user] = AnswerPair(index, wasCorrect)
                if (wasCorrect) {
                    addStats(user.id, turnStart, true)
                }

                if (answers.size == players.size) {
                    endTurn()
                } else if (mechanics.showAnswersAfter != GameMechanics.NO_HINTS && start.elapsedNow() > mechanics.showAnswersAfter) {
                    message.edit {
                        embed {
                            addQuestion(question, false)
                            addPlayers(answers, true)
                        }
                    }
                }
            }
        }
    }

    // Players that were too dumb to answer
    failRemainingPlayers(turnStart, answers)

    message.edit {
        embed {
            addQuestion(question, false)
            addPlayers(answers)
        }
    }
    uiMessage.delete()

    delay(3.seconds)
}

private fun <Player : MultipleChoicePlayer> MultipleChoiceGame<Player, *, *>.failRemainingPlayers(
    turnStart: Instant,
    answers: MutableMap<UserBehavior, AnswerPair>,
) {
    players.forEach {
        if (!answers.containsKey(it.user)) {
            mechanics.pointsDistributor.removePoints(it.user.gamePlayer)
            addStats(it.user.id, turnStart, false)
        }
    }
}

internal suspend fun MultipleChoiceGame<*, *, *>.translateInternally(user: UserBehavior, key: String) =
    translateInternally(module.bot.getLocale(thread.asChannel(), user.asUser()), key)

internal fun MultipleChoiceGame<*, *, *>.translateInternally(locale: Locale, key: String) =
    translationsProvider.translate(
        key, locale, "multiple_choice"
    )

internal suspend fun MultipleChoiceGame<*, *, *>.translateInternally(key: String) =
    translationsProvider.translate(
        key, locale(), "multiple_choice"
    )
