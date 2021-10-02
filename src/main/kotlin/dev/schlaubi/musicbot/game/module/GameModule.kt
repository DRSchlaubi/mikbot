package dev.schlaubi.musicbot.game.module

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import com.kotlindiscord.kord.extensions.extensions.slashCommandCheck
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.TextChannel
import dev.schlaubi.musicbot.game.AbstractGame
import dev.schlaubi.musicbot.game.GameStats
import dev.schlaubi.musicbot.game.Player
import dev.schlaubi.musicbot.module.SubCommandModule
import dev.schlaubi.musicbot.module.settings.BotUser
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KProperty1

/**
 * Abstract module for a game module.
 *
 * @param P the [Player] type
 * @param G the [AbstractGame] type
 *
 * @see SubCommandModule
 */
abstract class GameModule<P : Player, G : AbstractGame<P>> : SubCommandModule() {

    private val games = mutableMapOf<Snowflake, G>()

    override val commandName: String
        get() = name

    /**
     * The [BotUser] property for the games stats.
     */
    abstract val gameStats: KProperty1<BotUser, GameStats?>

    /**
     * The [ThreadChannelBehavior] of this [ApplicationCommandContext]
     */
    val ApplicationCommandContext.textChannel: TextChannel
        get() = runBlocking { channel.asChannel() as TextChannel }

    final override suspend fun overrideSetup() {
        slashCommandCheck { anyGuild() }
        gameSetup()
    }

    /**
     * Finds a game by its [threadId].
     */
    fun findGame(threadId: Snowflake): G? = games[threadId]

    /**
     * Creates a new game for [host].
     * @param welcomeMessage the head message in [thread]
     * @param thread the [ThreadChannelBehavior] the game is in
     * @param translationsProvider the [TranslationsProvider] to use
     */
    fun newGame(
        host: UserBehavior,
        welcomeMessage: Message,
        thread: ThreadChannelBehavior,
        translationsProvider: TranslationsProvider
    ) {
        games[thread.id] = obtainGame(host, welcomeMessage, thread, translationsProvider)
    }

    /**
     * Creates a new [Game][G] hosted by [host].
     * @param welcomeMessage the head message in [thread]
     * @param thread the [ThreadChannelBehavior] the game is in
     * @param translationsProvider the [TranslationsProvider] to use
     */
    abstract fun obtainGame(
        host: UserBehavior,
        welcomeMessage: Message,
        thread: ThreadChannelBehavior,
        translationsProvider: TranslationsProvider
    ): G

    /**
     * Additional setup calls.
     */
    protected open suspend fun gameSetup() = Unit

    /**
     * Translates [key] for the game bundle.
     */
    suspend fun CommandContext.translateGlobal(key: String) = translate(key, "games")

    /**
     * Translates [key] for the game bundle.
     */
    fun CheckContext<*>.translateGlobal(key: String) = translate(key, "games")
}
