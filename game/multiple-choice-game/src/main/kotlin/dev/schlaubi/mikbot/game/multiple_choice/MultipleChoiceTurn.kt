package dev.schlaubi.mikbot.game.multiple_choice

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
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

internal suspend fun <Q : Question> MultipleChoiceGame<*, Q, *>.turn(question: Q) {
    val allAnswers = question.allAnswers.filter { it.isNotBlank() }

    val turnStart = Clock.System.now()
    val message = thread.createMessage {
        embed {
            addQuestion(question, true)
        }
        actionRow {
            allAnswers.forEachIndexed { index, name ->
                interactionButton(ButtonStyle.Secondary, "choose_$index") {
                    label = (name as String?)?.take(80)
                }
            }
        }
        questionUI(question)
    }

    val answers = mutableMapOf<UserBehavior, Boolean>()

    // coroutineScope suspends until all child coroutines are dead
    // That way we can cancel all children at once
    coroutineScope {
        var job: Job? = null
        fun endTurn() {
            job!!.cancel()
        }

        job = launch {
            val liveMessage = message.componentLive()
            launch { // this blocks this scope until we cancel it
                delay(30.seconds)
                endTurn()
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
                answers[user] = wasCorrect
                if (wasCorrect) {
                    addStats(user.id, turnStart, true)
                }

                if (answers.size == players.size) {
                    endTurn()
                } else {
                    message.edit {
                        embed {
                            addPlayers(answers)
                            addQuestion(question, true)
                        }
                    }
                }
            }
        }
    }

    // Players that were too dumb to answer
    failRemainingPlayers(turnStart, answers)

    message.edit {
        components = mutableListOf()
        embed {
            addQuestion(question, false)
            addPlayers(answers)
        }
    }

    delay(3.seconds)
}

private fun MultipleChoiceGame<*, *, *>.failRemainingPlayers(
    turnStart: Instant,
    answers: MutableMap<UserBehavior, Boolean>,
) {
    players.forEach {
        if (!answers.containsKey(it.user)) {
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
