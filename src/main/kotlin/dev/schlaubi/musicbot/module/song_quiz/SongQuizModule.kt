package dev.schlaubi.musicbot.module.song_quiz

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.game.GameStats
import dev.schlaubi.musicbot.game.module.GameModule
import dev.schlaubi.musicbot.game.module.commands.leaderboardCommand
import dev.schlaubi.musicbot.game.module.commands.profileCommand
import dev.schlaubi.musicbot.game.module.commands.startGameCommand
import dev.schlaubi.musicbot.game.module.commands.stopGameCommand
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.module.music.checks.joinSameChannelCheck
import dev.schlaubi.musicbot.module.music.player.queue.PLAYLIST_PATTERN
import dev.schlaubi.musicbot.module.music.player.queue.getPlaylist
import dev.schlaubi.musicbot.module.settings.BotUser
import dev.schlaubi.musicbot.module.song_quiz.game.SongQuizGame
import dev.schlaubi.musicbot.module.song_quiz.game.SongQuizPlayer
import dev.schlaubi.musicbot.utils.extension
import dev.schlaubi.musicbot.utils.safeGuild
import kotlin.reflect.KProperty1

open class SongQuizSizeArguments : Arguments() {
    val size by defaultingInt("size", "How many songs of the playlist the game should ask", 25)
}

open class SongQuizPlaylistArguments : SongQuizSizeArguments() {
    val playlist by string("playlist", "The URL to the spotify playlist you want to quiz about")

    init {
        args.reverse()
    }
}

class SongQuizModule : GameModule<SongQuizPlayer, SongQuizGame>() {
    override val name: String = "song_quiz"
    override val gameStats: KProperty1<BotUser, GameStats?> = BotUser::quizStats
    override val bundle: String = "song_quiz"
    private val musicModule: MusicModule by extension()

    override suspend fun gameSetup() {
        startGameCommand(
            "shit",
            "Quiz about music I was listening to back in 2017",
            "https://open.spotify.com/playlist/6s2CcoPOwavAGSqnypq4Pt"
        )
        startGameCommand(
            "charts",
            "Quiz about the 2021 charts",
            "https://open.spotify.com/playlist/3xMQTDLOIGvj3lWH5e5x6F"
        )
        startGameCommand(
            "playlist",
            "Quiz about a specific playlist",
            ::SongQuizPlaylistArguments,
            SongQuizPlaylistArguments::playlist
        )
        stopGameCommand()
        leaderboardCommand("commands.song_quiz.leaderboard.page.title")
        profileCommand()
    }

    private suspend fun startGameCommand(
        name: String,
        description: String,
        playlistUrl: String
    ) = startGameCommand(name, description, ::SongQuizSizeArguments) { playlistUrl }

    private suspend fun <A : SongQuizSizeArguments> startGameCommand(
        name: String,
        description: String,
        arguments: () -> A,
        playlistArgument: A.() -> String
    ) = this@SongQuizModule.startGameCommand(
        "song_quiz.game.title", "song-quiz",
        arguments,
        findGame@{ message, thread ->
            val player = musicModule.getMusicPlayer(safeGuild)
            val playlistUrl = this.arguments.playlistArgument()
            val matchedPlaylist = PLAYLIST_PATTERN.find(playlistUrl)
            if (matchedPlaylist == null) {
                respond {
                    content = translate("commands.song_quiz.start_game.not_a_spotify_url")
                }
                return@findGame null
            }
            val (playlistId) = matchedPlaylist.destructured
            val playlist = getPlaylist(playlistId)
            if (playlist == null) {
                respond {
                    content = translate("commands.song_quiz.start_game.not_found")
                }
                return@findGame null
            }

            SongQuizGame(
                user,
                this@SongQuizModule,
                this.arguments.size.coerceAtMost(playlist.tracks.items.size),
                player,
                playlist,
                thread,
                message,
                translationsProvider
            )
        },
        { joinSameChannelCheck(bot) },
        name,
        description
    )
}
