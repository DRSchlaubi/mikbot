package dev.schlaubi.mikmusic.playlist.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikmusic.player.QueuedTrack
import dev.schlaubi.mikmusic.player.queue.QueueOptions
import dev.schlaubi.mikmusic.player.queue.findTracks
import dev.schlaubi.mikmusic.playlist.Playlist
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase
import org.litote.kmongo.newId

class PlaylistSaveArguments : Arguments(), QueueOptions {
    val name by string {
        name = "name"
        description = "The name of the playlist"
    }
    val public by defaultingBoolean {
        name = "public"
        description = "Whether this playlist is supposed to be public or not"
        defaultValue = false
    }
    val importFrom by optionalString {
        name = "import_from"
        description = "Link to a Track source (YouTube Video/Playlist, Spotify Song/Playlist/Album) to import"
    }
    override val query: String
        get() = importFrom ?: error("Cannot find tracks if importFrom is not specified")
    override val force: Boolean = false
    override val top: Boolean = false
    override val soundcloud: Boolean = false
}

fun PlaylistModule.saveCommand() = ephemeralSubCommand(::PlaylistSaveArguments) {
    name = "create"
    description = "Creates a new playlist"

    action {
        if (musicPlayer.playingTrack == null && arguments.importFrom == null) {
            respond {
                content = translate("music.checks.not_playing")
            }
        }

        checkName(arguments.name, arguments.public) {
            val tracks = if (arguments.importFrom == null) {
                (listOfNotNull(musicPlayer.playingTrack) + musicPlayer.queuedTracks).map(QueuedTrack::track)
            } else {
                findTracks(musicPlayer, false)?.tracks ?: return@action
            }

            val playlist = Playlist(
                newId(),
                user.id,
                arguments.name,
                tracks,
                arguments.public
            )

            PlaylistDatabase.collection.save(playlist)

            respond {
                content = translate("commands.playlist.save.saved", arrayOf(arguments.name, tracks.size))
            }
        }
    }
}
