package dev.schlaubi.mikbot.game.music_quiz

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.editingPaginator
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.util.forList
import dev.schlaubi.mikmusic.checks.musicQuizAntiCheat
import dev.schlaubi.mikmusic.util.musicModule

class RemoveArguments : Arguments() {
    val position by int("position", "The position of the song to remove")
}

suspend fun SongQuizModule.likedSongsCommand() = ephemeralSlashCommand {
    name = "song-likes"
    description = "Allows you to view your favourite song-quiz songs"

    ephemeralSubCommand {
        name = "show"
        description = "Shows all songs"

        check {
            musicQuizAntiCheat(musicModule)
        }

        action {
            val songs = MusicQuizDatabase.likedSongs.findOneById(user.id)
            if (songs?.songs.isNullOrEmpty()) {
                respond {
                    content = translate("commands.song_likes.empty")
                }
                return@action
            }

            editingPaginator {
                forList(
                    user,
                    songs!!.songs,
                    { "[${it.name} - ${it.artist}](${it.url})" },
                    { current, all -> translate("commands.song_likes.show.title", arrayOf(current, all)) }
                )
            }.send()
        }
    }

    ephemeralSubCommand(::RemoveArguments) {
        name = "remove"
        description = "Removes a liked song"

        check {
            musicQuizAntiCheat(musicModule)
        }

        action {
            val songs = MusicQuizDatabase.likedSongs.findOneById(user.id)
            if (songs == null) {
                respond {
                    content = translate("commands.song_likes.empty")
                }
                return@action
            }

            val newSongList = songs.songs.toMutableList()
            if (arguments.position > songs.songs.size) {
                respond {
                    content = translate("commands.song_likes.remove.out_of_bounds")
                }
                return@action
            }
            val removedSong = newSongList.removeAt(arguments.position - 1)

            val newSongs = songs.copy(songs = newSongList)
            MusicQuizDatabase.likedSongs.save(newSongs)

            respond {
                translate("commands.song_likes.removed", arrayOf(removedSong.name))
            }
        }
    }
}
