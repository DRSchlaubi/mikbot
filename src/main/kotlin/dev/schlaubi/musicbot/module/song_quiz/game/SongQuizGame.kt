package dev.schlaubi.musicbot.module.song_quiz.game

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.EphemeralFollowupMessage
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.musicbot.game.AbstractGame
import dev.schlaubi.musicbot.game.GameStats
import dev.schlaubi.musicbot.game.module.GameModule
import dev.schlaubi.musicbot.module.settings.BotUser
import kotlin.reflect.KProperty1

class SongQuizGame(
    host: UserBehavior,
    module: GameModule<SongQuizPlayer, out AbstractGame<SongQuizPlayer>>,
    override val thread: ThreadChannelBehavior,
    override val welcomeMessage: Message,
    override val translationsProvider: TranslationsProvider
) : AbstractGame<SongQuizPlayer>(host, module) {
    override val playerRange: IntRange = 1..10
    override val wonPlayers: List<SongQuizPlayer> = mutableListOf()
    override val bundle: String = "song_quiz"

    override fun EmbedBuilder.addWelcomeMessage() {
        TODO("Not yet implemented")
    }

    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralInteractionResponseBehavior,
        loading: EphemeralFollowupMessage
    ): SongQuizPlayer {
        TODO("Not yet implemented")
    }

    override suspend fun runGame() {
        TODO("Not yet implemented")
    }

    override fun BotUser.applyStats(stats: GameStats): BotUser = copy(quizStats = stats)
}
