package dev.schlaubi.mikbot.game.multiple_choice

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.Message
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import dev.schlaubi.mikbot.game.multiple_choice.mechanics.GameMechanics
import dev.schlaubi.mikbot.game.multiple_choice.player.MultipleChoicePlayer
import dev.schlaubi.mikbot.game.multiple_choice.player.addStats
import dev.schlaubi.mikbot.plugin.api.util.componentLive
import dev.schlaubi.mikbot.plugin.api.util.convertToISO
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
    delay(answerDelay)
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

    val answers = mutableMapOf<UserBehavior, AnswerContext>()

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
                    if (answers.isNotEmpty()) {
                        editMessage(message, question, answers)
                    }
                }
            }

            liveMessage.onInteraction {
                val event = this
                if (handle(question)) return@onInteraction // custom event handler

                val user = interaction.user
                val player = interaction.gamePlayer
                if (player == null) {
                    interaction.respondEphemeral {
                        content = translateInternally(event, "multiple_choice.game.not_in_game")
                    }
                    return@onInteraction
                }
                if (answers.containsKey(user)) {
                    interaction.respondEphemeral {
                        content = translateInternally(event, "multiple_choice.game.already_submitted")
                    }
                    return@onInteraction
                }
                val response = interaction.deferEphemeralMessageUpdate()
                val index = interaction.componentId.substringAfter("choose_").toInt()
                val name = allAnswers[index]
                val wasCorrect = name == question.correctAnswer
                val points = if (wasCorrect) {
                    mechanics.pointsDistributor.awardPoints(player, start.elapsedNow())
                } else {
                    -mechanics.pointsDistributor.removePoints(player)
                }
                answers[user] = AnswerContext(index, wasCorrect, this, response, points, this@turn)
                if (wasCorrect) {
                    addStats(user.id, turnStart, true)
                }

                if (answers.size == players.size) {
                    endTurn()
                } else if (mechanics.showAnswersAfter != GameMechanics.NO_HINTS && start.elapsedNow() > mechanics.showAnswersAfter) {
                    editMessage(message, question, answers)
                }
            }
        }
    }

    // Players that were too dumb to answer
    failRemainingPlayers(turnStart, answers)

    message.edit {
        embed {
            addQuestion(question, false)
            addPlayers(answers, game = this@turn)
        }
    }
    answers.forEach { (user, answer) ->
        launch {
            mechanics.pointsDistributor.sendPointMessage(user, answer)
        }
    }
    uiMessage.delete()

    delay(3.seconds)
}

private suspend fun <Q : Question> MultipleChoiceGame<*, Q, *>.editMessage(
    message: Message,
    question: Q,
    answers: MutableMap<UserBehavior, AnswerContext>,
) {
    message.edit {
        embed {
            addQuestion(question, true)
            addPlayers(answers, false, game = this@editMessage)
        }
    }
}

private fun <Player : MultipleChoicePlayer> MultipleChoiceGame<Player, *, *>.failRemainingPlayers(
    turnStart: Instant,
    answers: MutableMap<UserBehavior, AnswerContext>,
) {
    players.forEach {
        if (!answers.containsKey(it.user)) {
            mechanics.pointsDistributor.removePoints(it.user.gamePlayer)
            addStats(it.user.id, turnStart, false)
        }
    }
}

internal suspend fun MultipleChoiceGame<*, *, *>.translateInternally(
    user: UserBehavior,
    key: String,
    vararg parameters: Any?,
) =
    translateInternally(module.bot.getLocale(thread.asChannel(), user.asUser()), key, *parameters)

internal suspend fun MultipleChoiceGame<*, *, *>.translateInternally(
    event: InteractionCreateEvent,
    key: String,
    vararg parameters: Any?,
): String {
    val locale =
        event.interaction.locale?.convertToISO()?.asJavaLocale() ?: return translateInternally(
            event.interaction.user,
            key,
            *parameters
        )

    return translateInternally(locale, key, *parameters)
}

@Suppress("UNCHECKED_CAST")
internal fun MultipleChoiceGame<*, *, *>.translateInternally(locale: Locale, key: String, vararg parameters: Any?) =
    translationsProvider.translate(
        key, locale, "multiple_choice", parameters as Array<Any?>
    )

@Suppress("UNCHECKED_CAST")
internal suspend fun MultipleChoiceGame<*, *, *>.translateInternally(key: String, vararg parameters: Any?) =
    translationsProvider.translate(
        key, locale(), "multiple_choice", parameters as Array<Any?>
    )
