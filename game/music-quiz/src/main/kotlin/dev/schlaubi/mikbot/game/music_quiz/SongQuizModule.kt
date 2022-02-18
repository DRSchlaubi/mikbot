package dev.schlaubi.mikbot.game.music_quiz

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.api.module.commands.leaderboardCommand
import dev.schlaubi.mikbot.game.api.module.commands.profileCommand
import dev.schlaubi.mikbot.game.api.module.commands.startGameCommand
import dev.schlaubi.mikbot.game.api.module.commands.stopGameCommand
import dev.schlaubi.mikbot.game.multiple_choice.player.MultipleChoicePlayer
import dev.schlaubi.mikbot.game.music_quiz.game.SongQuizGame
import dev.schlaubi.mikbot.game.music_quiz.game.SongQuizPlayer
import dev.schlaubi.mikbot.game.music_quiz.game.TrackContainer
import dev.schlaubi.mikbot.plugin.api.util.extension
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikmusic.checks.joinSameChannelCheck
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.player.queue.PLAYLIST_PATTERN
import dev.schlaubi.mikmusic.player.queue.getPlaylist
import org.litote.kmongo.coroutine.CoroutineCollection

open class SongQuizSizeArguments : Arguments() {
    val size by defaultingInt {
        name = "size"
        description = "How many songs of the playlist the game should ask"
        defaultValue = 25
    }
}

open class SongQuizPlaylistArguments : SongQuizSizeArguments() {
    val playlist by string {
        name = "playlist"
        description = " The URL to the spotify playlist you want to quiz about"
    }

    init {
        // Fix optional argument size being before required argument playlist
        args.reverse()
    }
}

class SongQuizModule : GameModule<MultipleChoicePlayer, SongQuizGame>() {
    override val name: String = "song-quiz"
    override val gameStats: CoroutineCollection<UserGameStats> = MusicQuizDatabase.stats
    override val bundle: String = "song_quiz"
    private val musicModule: MusicModule by extension()

    override suspend fun gameSetup() {
        spotifyPlaylistSongs()
        startGameCommand(
            "playlist",
            "Quiz about a specific playlist",
            ::SongQuizPlaylistArguments,
            SongQuizPlaylistArguments::playlist
        )
        stopGameCommand()
        leaderboardCommand("commands.song_quiz.leaderboard.page.title")
        profileCommand()
        likedSongsCommand()
    }

    suspend fun startGameCommand(
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
        prepareData@{
            val playlistUrl = this.arguments.playlistArgument()
            val matchedPlaylist = PLAYLIST_PATTERN.find(playlistUrl)
            if (matchedPlaylist == null) {
                respond {
                    content = translate("commands.song_quiz.start_game.not_a_spotify_url")
                }
                return@prepareData null
            }
            val (playlistId) = matchedPlaylist.destructured
            val playlist = getPlaylist(playlistId)
            if (playlist == null) {
                respond {
                    content = translate("commands.song_quiz.start_game.not_found")
                }
                return@prepareData null
            }

            TrackContainer(playlist, this.arguments.size) {
                if (it == 1) {
                    respond {
                        translate("commands.song_quiz.start.rate_limit")
                    }
                }
            }
        },
        { trackContainer, message, thread ->
            val game = SongQuizGame(
                user,
                this@SongQuizModule,
                this.arguments.size.coerceAtMost(trackContainer.spotifyPlaylist.tracks.items.size),
                musicModule.getMusicPlayer(safeGuild),
                trackContainer,
                thread,
                message,
                translationsProvider
            )

            val hostPlayer = SongQuizPlayer(user)
            game.players.add(hostPlayer)

            game
        },
        { joinSameChannelCheck(bot) },
        name,
        description
    )
}
