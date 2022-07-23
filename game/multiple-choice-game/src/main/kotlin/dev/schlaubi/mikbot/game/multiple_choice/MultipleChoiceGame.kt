package dev.schlaubi.mikbot.game.multiple_choice

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.mikbot.game.api.AbstractGame
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.multiple_choice.mechanics.DefaultGameMechanics
import dev.schlaubi.mikbot.game.multiple_choice.mechanics.GameMechanics
import dev.schlaubi.mikbot.game.multiple_choice.player.MultipleChoicePlayer
import dev.schlaubi.mikbot.game.multiple_choice.player.Statistics
import dev.schlaubi.mikbot.plugin.api.util.componentLive
import dev.schlaubi.mikbot.plugin.api.util.convertToISO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

private const val requestStats = "request_stats"

/**
 * Abstract implementation of [AbstractGame] for multiple choice games.
 *
 * @property quizSize the size fo the quiz
 * @property questionContainer the providing [QuestionContainer]
 *
 * @param Player the [MultipleChoicePlayer] implementation used
 * @param Q the [Question] implementation used
 * @param QC the [QuestionContainer] implementaiton used
 * @property answerDelay delay [Duration] before answers are accepted
 */
abstract class MultipleChoiceGame<Player : MultipleChoicePlayer, Q : Question, QC : QuestionContainer<Q>>(
    host: UserBehavior,
    module: GameModule<Player, AbstractGame<Player>>,
    val quizSize: Int,
    val questionContainer: QC,
    internal val mechanics: GameMechanics<Player> = DefaultGameMechanics(),
) : AbstractGame<Player>(host, module) {
    override val playerRange: IntRange = 1..10
    open val answerDelay: Duration = 0.milliseconds
    internal val gameStats = mutableMapOf<Snowflake, Statistics>()
    override val wonPlayers: List<Player>
        get() =
            with(mechanics.pointsDistributor) {
                players.sortByRank()
            }

    override suspend fun onRejoin(event: ComponentInteractionCreateEvent, player: Player) {
        event.interaction.deferEphemeralMessageUpdate()
    }

    /**
     * Game loop (please call super when overriding).
     */
    override suspend fun runGame() {
        welcomeMessage.edit { components = mutableListOf() }
        val iterator = questionContainer.iterator()
        while (iterator.hasNext()) {
            askQuestion(iterator.next())
        }
    }

    /**
     * Turn logic, runs once per question asked (please call super when overriding).
     */
    open suspend fun askQuestion(question: Q) = turn(question)

    /**
     * Add additional features to the question ui
     */
    open suspend fun MessageCreateBuilder.questionUI(question: Q) = Unit

    /**
     * Handle additional interactions on questions.
     *
     * @return whether the interaction was handled or not
     */
    open suspend fun ComponentInteractionCreateEvent.handle(question: Q): Boolean = false

    override suspend fun end() {
        doUpdateWelcomeMessage()
        launch {
            endStats()
        }
    }

    private suspend fun endStats() {
        if (players.isNotEmpty() && running) {
            val message = thread.createMessage {
                embed {
                    title = translateInternally("game.final_results")

                    addGameEndEmbed(this@MultipleChoiceGame)
                }

                actionRow {
                    interactionButton(ButtonStyle.Primary, requestStats) {
                        label = translateInternally("game.request_stats")
                    }
                }
            }
            val live = message.componentLive()
            live.onInteraction {
                val event = this
                val user = interaction.user
                val winner = wonPlayers.firstOrNull()?.user
                val statistics = gameStats[interaction.user.id]
                interaction.respondEphemeral {
                    if (statistics == null) {
                        content = translateInternally(event, "multiple_choice.game.not_in_game")
                    } else if (user.id == winner?.id) {
                        content = translateInternally(event, "multiple_choice.game.won")
                    } else {
                        embed {
                            addUserStats(
                                user,
                                gameStats[user.id] ?: Statistics(
                                    0,
                                    emptyList(), quizSize
                                ),
                                mechanics.pointsDistributor.retrievePointsForPlayer(user.gamePlayer),
                                interaction.locale?.convertToISO()?.asJavaLocale() ?: Locale.ENGLISH,
                                this@MultipleChoiceGame
                            )
                        }
                    }
                }
            }

            delay(1.minutes)
            message.edit { components = mutableListOf() }
            live.cancel()
        }
    }

    /**
     * Adds a [question] to an [EmbedBuilder].
     *
     * @param hideCorrectAnswer whether this should hide the correct answer or not
     */
    abstract suspend fun EmbedBuilder.addQuestion(question: Q, hideCorrectAnswer: Boolean)

    override suspend fun EmbedBuilder.addWinnerGamecard() = addGameEndEmbed(this@MultipleChoiceGame)
}
