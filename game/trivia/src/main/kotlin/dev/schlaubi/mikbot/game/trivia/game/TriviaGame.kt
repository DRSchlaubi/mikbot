package dev.schlaubi.mikbot.game.trivia.game

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.FollowupMessage
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.mikbot.game.api.AutoJoinableGame
import dev.schlaubi.mikbot.game.api.Rematchable
import dev.schlaubi.mikbot.game.multiple_choice.MultipleChoiceGame
import dev.schlaubi.mikbot.game.multiple_choice.player.MultipleChoicePlayer
import dev.schlaubi.mikbot.game.trivia.QuestionContainer
import dev.schlaubi.mikbot.game.trivia.TriviaModule
import dev.schlaubi.mikbot.game.trivia.open_trivia.Question
import io.ktor.util.*
import java.util.*

private const val REQUEST_RAW = "request_raw"

class TriviaGame(
    private val locale: Locale,
    override val thread: ThreadChannelBehavior,
    override val welcomeMessage: Message,
    override val translationsProvider: TranslationsProvider,
    host: UserBehavior,
    module: TriviaModule,
    quizSize: Int,
    questionContainer: QuestionContainer
) : MultipleChoiceGame<MultipleChoicePlayer, TriviaQuestion, QuestionContainer>(
    host,
    module.asType,
    quizSize,
    questionContainer
),
    Rematchable<MultipleChoicePlayer, TriviaGame>,
    AutoJoinableGame<MultipleChoicePlayer> {
    override val rematchThreadName: String = "trivia-rematch"

    override fun EmbedBuilder.addWelcomeMessage() {
        if (questionContainer.category != null) {
            field {
                name = translate("trivia.question.category")
                value = translate("trivia.question.category." + questionContainer.category!!.translationName)
                inline = true
            }
        }

        if (questionContainer.difficulty != null) {
            field {
                name = translate("trivia.question.difficulty")
                value = translate("trivia.question.difficulty." + questionContainer.difficulty!!.translationName)
                inline = true
            }
        }

        if (questionContainer.type != null) {
            field {
                name = translate("trivia.question.type")
                value = translate("trivia.question.type." + questionContainer.type!!.translationName)
                inline = true
            }
        }

        footer {
            text = "Powered by Open Trivia & Google"
        }
    }

    override fun obtainNewPlayer(user: User): MultipleChoicePlayer = MultipleChoicePlayer(user)

    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralInteractionResponseBehavior,
        loading: FollowupMessage,
        userLocale: dev.kord.common.Locale?
    ): MultipleChoicePlayer = MultipleChoicePlayer(user)

    override suspend fun EmbedBuilder.addQuestion(question: TriviaQuestion, hideCorrectAnswer: Boolean) {
        addQuestion(question.parent, hideCorrectAnswer)
    }

    private fun EmbedBuilder.addQuestion(question: Question, hideCorrectAnswer: Boolean) {
        title = question.title

        field {
            name = translate("trivia.question.category")
            value = translate("trivia.question.category." + question.category.translationName)
            inline = true
        }

        field {
            name = translate("trivia.question.difficulty")
            value = translate("trivia.question.difficulty." + question.difficulty.translationName)
            inline = true
        }

        if (!hideCorrectAnswer) {
            field {
                name = translate("trivia.question.correct_answer")
                value = question.correctAnswer
                inline = false
            }
        }
    }

    override fun MessageCreateBuilder.questionUI(question: TriviaQuestion) {
        if (question.original != null) {
            actionRow {
                interactionButton(ButtonStyle.Primary, REQUEST_RAW) {
                    label = translate("trivia.game.request_raw")
                }
            }
        }
    }

    override suspend fun ComponentInteractionCreateEvent.handle(question: TriviaQuestion): Boolean {
        if (question.original != null && interaction.componentId == REQUEST_RAW) {
            interaction.respondEphemeral {
                embed {
                    addQuestion(question.original, true)
                }

                actionRow {
                    question.original.allAnswers.forEach {
                        interactionButton(ButtonStyle.Secondary, generateNonce()) {
                            label = it
                            disabled = true
                        }
                    }
                }
            }

            return true
        }

        return false
    }

    override suspend fun rematch(thread: ThreadChannelBehavior, welcomeMessage: Message): TriviaGame {
        val newQuestionContainer = QuestionContainer(
            questionContainer.size,
            questionContainer.difficulty,
            questionContainer.category,
            questionContainer.type,
            locale,
            translationsProvider,
            module as TriviaModule
        )

        return TriviaGame(
            locale,
            thread,
            welcomeMessage,
            translationsProvider,
            host,
            module,
            quizSize,
            newQuestionContainer
        ).apply {
            players.addAll(this@TriviaGame.players)
        }
    }

    private fun translate(key: String) = translationsProvider.translate(key, locale, module.bundle)
}
