package dev.schlaubi.mikbot.game.hangman.game

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.utils.waitFor
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.interaction.FollowupMessage
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.schlaubi.mikbot.game.api.AutoJoinableGame
import dev.schlaubi.mikbot.game.api.Rematchable
import dev.schlaubi.mikbot.game.api.SingleWinnerGame
import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikbot.game.google_emotes.*
import dev.schlaubi.mikbot.game.hangman.HangmanModule
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration.Companion.minutes

class HangmanGame(
    lastWinner: UserBehavior?,
    host: UserBehavior,
    module: HangmanModule,
    override val welcomeMessage: Message,
    override val thread: ThreadChannelBehavior,
    override val translationsProvider: TranslationsProvider,
) : SingleWinnerGame<HangmanPlayer>(host, module.asType),
    Rematchable<HangmanPlayer, HangmanGame>,
    AutoJoinableGame<HangmanPlayer> {
    override val rematchThreadName: String = "googologo-rematch"
    private val wordOwner = lastWinner ?: host
    override val playerRange: IntRange = 2..Int.MAX_VALUE
    private val gameCompleter by lazy { CompletableDeferred<Unit>() }
    private var state: GameState = GameState.WaitingForWord

    override fun obtainNewPlayer(user: User): HangmanPlayer = HangmanPlayer(user)

    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralInteractionResponseBehavior,
        loading: FollowupMessage,
        userLocale: dev.kord.common.Locale?
    ): HangmanPlayer = HangmanPlayer(user)

    private suspend fun retrieveWord(): String? {
        val wordOwner = players.first { it.user == wordOwner }
        players.remove(wordOwner)
        leftPlayers.add(wordOwner)
        val message = thread.createMessage(
            """${wordOwner.user.mention} you got randomly,
                |..., I mean not really randomly, much more specifically,
                |anyways,
                |you were selected to chose the word, so please send me a DM with your word of choice 
                |(unless it's longer than 102 chars) or shorter than 3 chars, then I don't want it.
                |And yes those numbers were arbitrarily chosen""".trimMargin()
        )

        val wordEvent = kord.waitFor<MessageCreateEvent>(1.minutes.inWholeMilliseconds) {
            // Check if message is from wordOwner
            this.message.channel.asChannelOfOrNull<DmChannel>()?.recipientIds?.contains(wordOwner.user.id) == true
        }
        message.delete()

        if (wordEvent == null) {
            thread.createMessage {
                content =
                    "Because ${wordOwner.user.mention} took too long this game is now over, what an egoistic idiot?"
            }
            softEnd()
            return null
        }

        val user = wordEvent.message.author!!
        val word = wordEvent.message.content
        if (word.length !in 3..102) {
            wordEvent.message.reply {
                content = translate("hangman.game.wrong_word")
            }

            thread.createMessage {
                content = """${wordOwner.user.mention} is too dumb to follow simple instructions, which made us think,
                        |they might be an Apple user, so if you know that's the case feel free to bully them, and if not
                        |bully them anyways, because they were to stupid to follow simple instructions
                    """.trimMargin()
            }
            softEnd()
            return null
        }

        wordEvent.message.reply {
            content = translate("hangman.word_accepted", word, thread.mention)
        }

        return word
    }

    private suspend fun startGame(scope: CoroutineScope, word: String) {
        val listener =
            kord.events.filterIsInstance<MessageCreateEvent>().filter { it.message.channelId == thread.id }.filter {
                val user = it.message.author ?: return@filter false
                players.any { event -> event.user == user }
            }.onEach(::onNewGuess).launchIn(scope)

        val state = GameState.Guessing(
            listener, word
        )
        this@HangmanGame.state = state

        welcomeMessage.edit {
            embeds = mutableListOf(state.toEmbed())
        }
        thread.createMessage("You can start guessing now!")
    }

    private suspend fun onNewGuess(event: MessageCreateEvent) = coroutineScope {
        launch { event.message.delete("Hangman input") }
        (state as? GameState.Guessing)?.mutex?.withLock {
            val guessingState = state as? GameState.Guessing ?: return@withLock
            val char = event.message.content.uppercase(Locale.ENGLISH).singleOrNull()
            state = if (char == null) {
                if (event.message.content.equals(guessingState.word, ignoreCase = true)) {
                    GameState.Done(players.first { it.user == event.message.author }, guessingState.word)
                } else {
                    guessingState.copy(blackList = guessingState.blackList + event.message.content)
                }
            } else {
                guessingState.copy(chars = guessingState.chars + char)
            }

            state.takeIfIsInstance<GameState.Guessing> {
                val guessedChars = chars.map { it.lowercase() }
                when {
                    guessedChars.containsAll(
                        word
                            .asSequence()
                            .map { it.lowercase() }
                            .filterNot { it.isBlank() } // you do not have to guess white spaces
                            .distinct()
                            .toList()
                    ) ->
                        state =
                            GameState.Done(players.first { it.user == event.message.author }, guessingState.word)
                    (wrongChars.size + blackList.size) >= maxTries ->
                        state =
                            GameState.Done(leftPlayers.first { it.user == wordOwner }, guessingState.word)
                    else -> welcomeMessage.edit {
                        embeds = mutableListOf(toEmbed())
                    }
                }
            }

            state.takeIfIsInstance<GameState.Done> {
                gameCompleter.complete(Unit)
                this@HangmanGame.winner = winner
            }
        }
    }

    private fun softEnd() = kord.launch { doEnd() }

    override suspend fun runGame() = coroutineScope {
        welcomeMessage.edit { components = mutableListOf() }
        val word = retrieveWord() ?: return@coroutineScope
        startGame(this, word)
        if (state is GameState.Guessing) {
            // wait for game to finish
            gameCompleter.await()
            cancel() // kill orphan coroutines, after game ended
        }
    }

    override suspend fun end() {
        state.close()
        // TODO: Make this a sticker
        if (state is GameState.Done && winner!!.user == wordOwner) {
            welcomeMessage.reply {
                content = "https://media.discordapp.net/stickers/861039079151763486.png"
            }
        }
    }

    override suspend fun EmbedBuilder.endEmbed(messageModifyBuilder: MessageModifyBuilder) {
        if (!running) return

        val word = (state as? GameState.HasWord)?.word ?: return
        if (winner!!.user == wordOwner) {
            description = "So no one was clever enough to guess it but the Word was: `$word`"
        } else {
            field {
                name = "Word"
                value = word
            }
        }
    }

    override suspend fun rematch(thread: ThreadChannelBehavior, welcomeMessage: Message): HangmanGame {
        val game = HangmanGame(
            winner!!.user, host, module as HangmanModule, welcomeMessage, thread, translationsProvider
        )
        val actualPlayers = players + HangmanPlayer(wordOwner)
        game.players.addAll(actualPlayers)
        return game
    }

    override suspend fun onRejoin(event: ComponentInteractionCreateEvent, player: HangmanPlayer) {
        event.interaction.acknowledgeEphemeralDeferredMessageUpdate()
    }

    companion object {
        val googologo = listOf(capitalG, redO, yellowO, smallG, redO, l, yellowO, smallG, redO)
        val maxTries = googologo.size
    }
}

sealed interface GameState {
    suspend fun close() = Unit

    object WaitingForWord : GameState

    interface HasWord {
        val word: String
    }

    data class Guessing(
        val listener: Job,
        override val word: String,
        val chars: Set<Char> = emptySet(),
        val blackList: Set<String> = emptySet(),
        val mutex: Mutex = Mutex()
    ) : GameState, HasWord {
        val wrongChars: Set<Char> = chars.filter { it !in word.uppercase(Locale.ENGLISH) }.toSet()
        override suspend fun close() {
            listener.cancel()
        }
    }

    data class Done(val winner: HangmanPlayer, override val word: String) : GameState, HasWord
}

inline fun <reified T : GameState> GameState.takeIfIsInstance(block: T.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    if (this is T) {
        block()
    }
}
