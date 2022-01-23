package dev.schlaubi.mikbot.game.googolplex.game

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.InteractionFollowup
import dev.kord.rest.builder.message.modify.embed
import dev.schlaubi.mikbot.game.api.*
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.google_emotes.googleLogoColor
import dev.schlaubi.mikbot.game.google_emotes.googleLogoWhite
import dev.schlaubi.mikbot.plugin.api.util.discordError

class GoogolplexGame(
    val size: Int,
    private val maxTries: Int,
    private val lastWinner: GoogolplexPlayer?,
    host: UserBehavior,
    module: GameModule<GoogolplexPlayer, out AbstractGame<GoogolplexPlayer>>,
    override val thread: ThreadChannelBehavior,
    override val welcomeMessage: Message,
    override val translationsProvider: TranslationsProvider
) : SingleWinnerGame<GoogolplexPlayer>(host, module), Rematchable<GoogolplexGame>, ControlledGame<GoogolplexPlayer> {
    override val rematchThreadName: String = "googolplex-rematch"
    override val playerRange: IntRange = 2 until 3

    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralInteractionResponseBehavior,
        loading: InteractionFollowup
    ): GoogolplexPlayer = GoogolplexPlayer(user, loading, ack)

    override suspend fun runGame() {
        val startingUser = lastWinner?.user ?: host
        val startingPlayer = startingUser.gamePlayer
        val guessingPlayer = (players - startingPlayer).first()

        val correctSequence = startingPlayer.awaitInitialSequence(this)
        startingPlayer.controls.edit {
            components = mutableListOf()
        }
        val existingGuesses = mutableListOf<String>()

        var done = false
        var tries = 0
        while (!done) {
            fun last() = existingGuesses.takeLast(25)
            val lastGuess = guessingPlayer.awaitSequence(
                size,
                translate(guessingPlayer.user, "googolplex.controls.request_guess", size)
            ) { _, current ->
                interaction.acknowledgeEphemeralDeferredMessageUpdate()
                updateGameStateMessage(last(), current)
            }
            existingGuesses += lastGuess.buildGuessUI(correctSequence)
            updateGameStateMessage(last())
            done = lastGuess == correctSequence || ++tries > 10
        }
        winner = if (tries <= maxTries) {
            guessingPlayer // only the guessing player can win
        } else {
            startingPlayer
        }
    }

    private suspend fun updateGameStateMessage(
        last: List<String>,
        current: List<ReactionEmoji>? = null
    ) {
        welcomeMessage.edit {
            val description = buildString {
                appendLine("**Guesses:**")

                if (last.isNotEmpty()) {
                    last.forEach {
                        appendLine(it)
                    }

                    if (current != null) {
                        appendLine()
                    }
                }

                current?.forEach {
                    append(it.mention)
                }
            }
            embed {
                this.description = description

                field {
                    name = "Legend"
                    value = """
                                    ${googleLogoWhite.mention} means that there is a correct item in the sequence
                                    ${googleLogoColor.mention} means that there is a correct item in the sequence at the correct position
                                    
                                    If there are wrong items in the sequence, these are just skipped in the hints section,
                                    so just because there is a ${googleLogoWhite.mention} at position 1, doesn't mean that, that item is correct
                    """.trimIndent()
                }
            }
        }
    }

    private fun List<ReactionEmoji>.buildGuessUI(correctSequence: List<ReactionEmoji>) = buildString {
        correctSequence.forEach {
            append(it.mention)
        }
        val hints = buildHintList(correctSequence)
        if (hints.isNotEmpty()) {
            append(" | ")
            buildHintList(correctSequence).forEach {
                append(it)
            }
        }
    }

    private fun List<ReactionEmoji>.buildHintList(correctSequence: List<ReactionEmoji>) =
        mapIndexedNotNull { index, item ->
            when (item) {
                correctSequence[index] -> googleLogoColor.mention
                in correctSequence -> googleLogoWhite.mention
                else -> null
            }
        }

    override suspend fun rematch(thread: ThreadChannelBehavior, welcomeMessage: Message): GoogolplexGame {
        val game = GoogolplexGame(
            size,
            maxTries,
            winner!!,
            host,
            module,
            thread,
            welcomeMessage,
            translationsProvider
        )
        if (!askForRematch(
                thread,
                game
            )
        ) {
            discordError("Game could not restart")
        }

        return game
    }
}
