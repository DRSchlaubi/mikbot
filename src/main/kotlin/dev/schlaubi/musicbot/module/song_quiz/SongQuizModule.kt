package dev.schlaubi.musicbot.module.song_quiz

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.entity.Message
import dev.schlaubi.musicbot.game.GameStats
import dev.schlaubi.musicbot.game.module.GameModule
import dev.schlaubi.musicbot.module.settings.BotUser
import dev.schlaubi.musicbot.module.song_quiz.game.SongQuizGame
import dev.schlaubi.musicbot.module.song_quiz.game.SongQuizPlayer
import kotlin.reflect.KProperty1

class SongQuizModule : GameModule<SongQuizPlayer, SongQuizGame>() {
    override val name: String = "song_quiz"
    override val gameStats: KProperty1<BotUser, GameStats?> = BotUser::quizStats

    override fun obtainGame(
        host: UserBehavior,
        welcomeMessage: Message,
        thread: ThreadChannelBehavior,
        translationsProvider: TranslationsProvider
    ): SongQuizGame {
        TODO("Not yet implemented")
    }
}
