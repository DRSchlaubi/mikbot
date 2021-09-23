package dev.schlaubi.musicbot.module.music.playlist.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.interactions.respond
import dev.schlaubi.musicbot.module.music.checks.anyMusicPlaying
import dev.schlaubi.musicbot.module.music.playlist.Playlist
import dev.schlaubi.musicbot.utils.database
import org.litote.kmongo.newId

class PlaylistSaveArguments : Arguments() {
    val name by string("name", "The name of the playlist")
    val public by defaultingBoolean("public", "Whether this playlist is supposed to be public or not", false)
}

fun PlaylistModule.saveCommand() = playlistSubCommand(::PlaylistSaveArguments) {
    name = "save"
    description = "Saves the current queueTracks as a playliste"

    check {
        anyMusicPlaying(musicModule)
    }

    action {
        checkName(arguments.name, arguments.public) {
            val tracks = listOfNotNull(musicPlayer.player.playingTrack) + musicPlayer.queuedTracks

            val playlist = Playlist(
                newId(),
                user.id,
                arguments.name,
                tracks,
                arguments.public
            )

            database.playlists.save(playlist)

            respond {
                content = translate("commands.playlist.save.saved", arrayOf(arguments.name, tracks.size))
            }
        }
    }
}
