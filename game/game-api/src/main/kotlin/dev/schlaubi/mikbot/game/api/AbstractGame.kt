package dev.schlaubi.mikbot.game.api

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Locale
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.channel.threads.edit
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.EphemeralFollowupMessage
import dev.kord.core.entity.interaction.FollowupMessage
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.rest.builder.message.modify.UserMessageModifyBuilder
import dev.kord.rest.builder.message.modify.embed
import dev.kord.x.emoji.Emojis
import dev.schlaubi.mikbot.game.api.events.interactionHandler
import dev.schlaubi.mikbot.game.api.events.watchThread
import dev.schlaubi.mikbot.game.api.module.GameModule
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.litote.kmongo.coroutine.CoroutineCollection
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.minutes

private val LOG = KotlinLogging.logger { }

const val resendControlsButton = "resend_controls"

/**
 * Abstract implementation of a game.
 *
 * @param T the [Player] type
 * @property players a list of all players in the game, which are still playing
 * @property thread the [ThreadChannelBehavior] the game is in
 * @property welcomeMessage the [Message] at the top of the thread
 * @property wonPlayers a list of [T] with all won players
 * @property bundle the translation bundle name
 * @property kord the kord instance to use
 * @property translationsProvider the [TranslationsProvider] used for translations
 */
abstract class AbstractGame<T : Player>(
    val host: UserBehavior,
    val module: GameModule<T, out AbstractGame<T>>
) : KoinComponent, CoroutineScope {
    val players: MutableList<T> = mutableListOf()
    override val coroutineContext: CoroutineContext =
        Dispatchers.IO + SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->
            LOG.error(throwable) { "An error occurred in Game $coroutineContext " }
        }
    protected val leftPlayers = mutableListOf<T>()

    abstract val playerRange: IntRange
    abstract val thread: ThreadChannelBehavior
    abstract val welcomeMessage: Message
    abstract val wonPlayers: List<T>
    open val isEligibleForStats = true
    val hostPlayer: T?
        get() = players.firstOrNull { it.user == host }
    val bundle: String
        get() = module.bundle
    private val statsCollection: CoroutineCollection<UserGameStats> get() = module.gameStats
    val kord: Kord get() = host.kord
    private var gameJob: Job? = null

    val safeRange get() = if (hostPlayer == null) (playerRange.first - 1) until playerRange.last else playerRange

    abstract val translationsProvider: TranslationsProvider

    /**
     * Whether the game is running or not.
     */
    var running = false
        private set

    /**
     * The [Player] who triggered this interaction if any.
     */
    val ComponentInteraction.gamePlayer: T?
        get() = players.firstOrNull { it.user == user }

    private val interactionListener: Job = interactionHandler()
    private val threadWatcher: Job = watchThread()

    private var silentEnd = false

    val UserBehavior.gamePlayer: T
        get() = players.first { it.user.id == id }

    /**
     * Require the game to be started to do this.
     */
    protected fun requireRunning() = check(running) { "Game has to be started to perform this action" }

    /**
     * Event handler for custom events on [welcomeMessage].
     */
    open suspend fun ComponentInteractionCreateEvent.onInteraction() = Unit

    /**
     * Adds the welcome message content to this embed builder (add game state to embed).
     */
    protected open fun EmbedBuilder.addWelcomeMessage() = Unit

    /**
     * Event handler called if [event] made [player] rejoin.
     */
    open suspend fun onRejoin(event: ComponentInteractionCreateEvent, player: T) = Unit

    /**
     * Event handler called when [player] joined the game.
     */
    open suspend fun onJoin(ack: EphemeralInteractionResponseBehavior, player: T) = Unit

    /**
     * Removes [player] from the game.
     */
    suspend fun doRemovePlayer(player: T) {
        players.remove(player)
        leftPlayers.add(player)
        removePlayer(player)
        if (running && players.isEmpty()) {
            doEnd()
        }
    }

    /**
     * Additional remove logic.
     */
    protected open suspend fun removePlayer(player: T) = Unit

    /**
     * Creates a new player for [user].
     *
     * @param ack the [EphemeralInteractionResponseBehavior] for the triggering interaction
     * @param loading the [EphemeralFollowupMessage] for the user who joined
     * @param userLocale the locale Discord included in the interaction
     */
    abstract suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralInteractionResponseBehavior,
        loading: FollowupMessage,
        userLocale: Locale?
    ): T

    /**
     * Updates the welcome message.
     */
    suspend fun doUpdateWelcomeMessage() {
        welcomeMessage.edit {
            if (this@AbstractGame is ControlledGame<*>) {
                addResendControlsButton()
            }

            if (running) {
                updateWelcomeMessage()
            } else {
                embed {
                    title = welcomeMessage.embeds.firstOrNull()?.title
                    addWelcomeMessage()
                    if (players.isNotEmpty() && fields.none { it.name == "Players" }) {
                        field {
                            name = "Players"
                            val mentions = players.map { it.user.mention }
                            val players = if ((this@AbstractGame as? ControlledGame<*>)?.supportsAutoJoin == true) {
                                listOf("(${host.mention})") + mentions
                            } else {
                                mentions
                            }

                            value = players.joinToString(", ")
                        }
                    }
                }
            }
            gameUI(this@AbstractGame)
        }
    }

    /**
     * Adds additional properties to the welcome message.
     */
    protected open suspend fun MessageModifyBuilder.updateWelcomeMessage() = Unit

    /**
     * Starts the game.
     */
    fun doStart() {
        launch {
            running = true
            coroutineScope {
                if (this@AbstractGame is ControlledGame<*>) {
                    welcomeMessage.edit {
                        addResendControlsButton()
                    }
                }

                gameJob = launch {
                    runGame()
                }
            }
            if (!silentEnd) {
                doEnd()
            }
        }
    }

    /**
     * Game function suspends for the whole game.
     */
    protected abstract suspend fun runGame()

    protected open suspend fun end() = Unit

    /**
     * Ends the game.
     */
    @Suppress("SuspendFunctionOnCoroutineScope") // we don't want this to have the same context
    suspend fun doEnd(abrupt: Boolean = false) = coroutineScope {
        if (gameJob?.isActive == true) {
            gameJob!!.cancel()
        }
        if (running) {
            launch {
                updateStats()
            }
        }
        running = !abrupt
        silentEnd = true
        val playersCopy = players
        if (this@AbstractGame is ControlledGame<*>) {
            playersCopy.forEach {
                (it as ControlledPlayer).controls.edit {
                    components = mutableListOf()
                    content = translateInternally(it.user, "game.controls.ended")
                }
            }
        }
        end()
        module.unregisterGame(thread.id)

        welcomeMessage.edit {
            components = mutableListOf()
            embed {
                description = "Winners: "

                if (running && wonPlayers.isNotEmpty()) {
                    repeat(3) {
                        winner(it)
                    }

                    addWinnerGamecard()
                } else {
                    description = "The game ended abruptly"
                }
                endEmbed(this@edit)
            }
            if (!abrupt) {
                rematchLogic()
            }
            modifyEndMessage()
        }

        launch {
            delay(2.minutes) // delay so users can discuss game end and click the stats button
            welcomeMessage.edit { components = mutableListOf() }
            thread.edit {
                reason = "Game ended"
                archived = true
            }
        }
        interactionListener.cancel()
        threadWatcher.cancel()
    }

    private suspend fun UserMessageModifyBuilder.rematchLogic() {
        if (this@AbstractGame is Rematchable<*>) {
            components(1.minutes) {
                publicButton {
                    label = "Rematch"
                    id = "rematch"

                    action {
                        val gameThread =
                            thread.parent.asChannelOf<TextChannel>().startPublicThread(rematchThreadName)
                        gameThread.addUser(user.id) // Add creator
                        val gameMessage = gameThread.createEmbed { description = "The game will begin shortly" }
                        gameMessage.pin(reason = "Game Welcome message")
                        val game = try {
                            rematch(gameThread, gameMessage)
                        } catch (ignored: DiscordRelayedException) {
                            return@action
                        }

                        @Suppress("UNCHECKED_CAST")
                        fun <T : AbstractGame<*>> GameModule<*, T>.registerGame(
                            id: Snowflake,
                            game: AbstractGame<*>
                        ) = registerGame(id, game as T)

                        module.registerGame(gameThread.id, game)

                        game.players.forEach {
                            gameThread.addUser(it.user.id)
                        }
                        game.welcomeMessage.edit { components = mutableListOf() }
                        respond {
                            content =
                                "A rematch has been started here (${gameThread.mention}), if you don't want to participate, just leave"
                        }
                        game.doStart()
                    }
                }
            }
        }
    }

    /**
     * Allows editing of the final embed.
     */
    protected open suspend fun EmbedBuilder.addWinnerGamecard() = Unit

    /**
     * Adds additional UI to the end embed.
     */
    protected open suspend fun EmbedBuilder.endEmbed(messageModifyBuilder: MessageModifyBuilder) = Unit

    private suspend fun updateStats() {
        if (!isEligibleForStats) {
            return
        }
        // Winning against yourself, doesn't count
        if ((players.size + leftPlayers.size) == 1) return
        val winner = wonPlayers.firstOrNull()
        if (winner != null) {
            update(winner) {
                copy(
                    wins = wins + 1,
                    ratio = (wins + 1).toDouble().div(wins + losses.coerceAtLeast(1)),
                    totalGamesPlayed = totalGamesPlayed + 1
                )
            }

            ((players + leftPlayers) - winner).forEach {
                update(it) {
                    copy(
                        losses = losses + 1,
                        ratio = wins.toDouble().div((losses + 1) + wins),
                        totalGamesPlayed = totalGamesPlayed + 1
                    )
                }
            }
        }
    }

    open fun MessageModifyBuilder.modifyEndMessage() = Unit

    private fun EmbedBuilder.winner(placeIndex: Int) {
        val player = wonPlayers.getOrNull(placeIndex) ?: return

        val medal = when (placeIndex) {
            0 -> Emojis.firstPlace
            1 -> Emojis.secondPlace
            2 -> Emojis.thirdPlace
            else -> null
        }

        description += "${medal?.toString() ?: (placeIndex + 1).toString()}${player.user.mention}"
    }
}
